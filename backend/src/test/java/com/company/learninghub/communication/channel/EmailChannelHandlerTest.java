package com.company.learninghub.communication.channel;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationOutboxEntry;
import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.email.EmailDeliveryResult;
import com.company.learninghub.communication.email.EmailMessage;
import com.company.learninghub.communication.email.EmailProvider;
import com.company.learninghub.communication.repository.CommunicationOutboxRepository;
import com.company.learninghub.communication.service.CommunicationEventSerializer;
import com.company.learninghub.communication.template.EmailTemplateRenderer;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailChannelHandlerTest {

    private static final Instant NOW = Instant.parse("2026-07-12T12:00:00Z");

    @Mock
    private CommunicationOutboxRepository outboxRepository;
    @Mock
    private EmailProvider emailProvider;
    @Mock
    private EmailTemplateRenderer emailTemplateRenderer;
    @Mock
    private UserRepository userRepository;

    private EmailChannelHandler handler;
    private CommunicationEventSerializer serializer;

    @BeforeEach
    void setUp() {
        CommunicationProperties properties = new CommunicationProperties();
        properties.getOutbox().setMaxRetries(3);
        serializer = new CommunicationEventSerializer(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new EmailChannelHandler(
                outboxRepository,
                serializer,
                properties,
                emailProvider,
                emailTemplateRenderer,
                userRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void enqueueSkipsDuplicateIdempotencyKey() {
        CommunicationEvent event = sampleEvent();
        when(outboxRepository.existsByIdempotencyKey(event.idempotencyKey() + ":EMAIL")).thenReturn(true);

        handler.enqueue(event);

        verify(outboxRepository, never()).save(any());
    }

    @Test
    void enqueuePersistsPendingOutboxEntry() {
        CommunicationEvent event = sampleEvent();
        when(outboxRepository.existsByIdempotencyKey(event.idempotencyKey() + ":EMAIL")).thenReturn(false);

        handler.enqueue(event);

        ArgumentCaptor<CommunicationOutboxEntry> captor = ArgumentCaptor.forClass(CommunicationOutboxEntry.class);
        verify(outboxRepository).save(captor.capture());
        CommunicationOutboxEntry saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(CommunicationOutboxStatus.PENDING);
        assertThat(saved.getChannel()).isEqualTo(CommunicationChannel.EMAIL);
        assertThat(saved.getPriority()).isEqualTo(CommunicationPriority.HIGH);
    }

    @Test
    void processEntryMarksSentWhenProviderSucceeds() {
        User recipient = activeUser();
        CommunicationEvent event = sampleEvent();
        CommunicationOutboxEntry entry = outboxEntry(event);
        EmailMessage emailMessage = new EmailMessage(
                "noreply@learninghub.local",
                recipient.getEmail(),
                "Certificate approved",
                "text body",
                "<p>html body</p>"
        );
        when(userRepository.findById(event.recipientUserId())).thenReturn(Optional.of(recipient));
        when(emailTemplateRenderer.buildEmailMessage(eq(recipient), any(CommunicationEvent.class))).thenReturn(emailMessage);
        when(emailProvider.send(emailMessage)).thenReturn(EmailDeliveryResult.success("msg-1"));
        when(outboxRepository.save(entry)).thenReturn(entry);

        handler.processEntry(entry);

        assertThat(entry.getStatus()).isEqualTo(CommunicationOutboxStatus.SENT);
        assertThat(entry.getProcessedAt()).isEqualTo(NOW);
    }

    @Test
    void processEntrySchedulesRetryWhenProviderFails() {
        User recipient = activeUser();
        CommunicationEvent event = sampleEvent();
        CommunicationOutboxEntry entry = outboxEntry(event);
        EmailMessage emailMessage = new EmailMessage(
                "noreply@learninghub.local",
                recipient.getEmail(),
                "Certificate approved",
                "text body",
                "<p>html body</p>"
        );
        when(userRepository.findById(event.recipientUserId())).thenReturn(Optional.of(recipient));
        when(emailTemplateRenderer.buildEmailMessage(eq(recipient), any(CommunicationEvent.class))).thenReturn(emailMessage);
        when(emailProvider.send(emailMessage)).thenReturn(EmailDeliveryResult.failure("smtp down"));
        when(outboxRepository.save(entry)).thenReturn(entry);

        handler.processEntry(entry);

        assertThat(entry.getStatus()).isEqualTo(CommunicationOutboxStatus.FAILED);
        assertThat(entry.getRetryCount()).isEqualTo(1);
        assertThat(entry.getAvailableAt()).isAfter(NOW);
    }

    private CommunicationEvent sampleEvent() {
        UUID recipientId = UUID.randomUUID();
        return new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.CERTIFICATE_APPROVED,
                NOW,
                UUID.randomUUID(),
                recipientId,
                new CommunicationEntityRef("CERTIFICATE_SUBMISSION", UUID.randomUUID(), "/submissions"),
                Map.of(
                        "title", "Certificate approved",
                        "message", "Your certificate was approved."
                ),
                Set.of(CommunicationChannel.EMAIL),
                CommunicationPriority.HIGH,
                "CERTIFICATE_APPROVED:entity:recipient"
        );
    }

    private CommunicationOutboxEntry outboxEntry(CommunicationEvent event) {
        return new CommunicationOutboxEntry(
                event.idempotencyKey() + ":EMAIL",
                CommunicationChannel.EMAIL,
                event.type(),
                serializer.serialize(event),
                event.priority(),
                NOW,
                NOW
        );
    }

    private User activeUser() {
        User user = new User("E001", "employee@learninghub.local", "Jane Doe", "hash");
        user.setActive(true);
        return user;
    }
}
