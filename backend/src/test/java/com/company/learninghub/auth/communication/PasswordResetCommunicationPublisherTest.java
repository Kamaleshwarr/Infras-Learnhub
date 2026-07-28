package com.company.learninghub.auth.communication;

import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.service.CommunicationService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetCommunicationPublisherTest {

    private static final Instant NOW = Instant.parse("2026-07-28T09:00:00Z");

    @Mock
    private CommunicationService communicationService;

    private PasswordResetCommunicationPublisher publisher;
    private User user;

    @BeforeEach
    void setUp() {
        publisher = new PasswordResetCommunicationPublisher(
                communicationService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
    }

    @Test
    void publishPasswordResetRequestedPublishesEmailOnlyEvent() {
        UUID tokenId = UUID.randomUUID();
        String resetUrl = "http://localhost:5173/reset-password?token=abc123";

        publisher.publishPasswordResetRequested(user, tokenId, resetUrl, Duration.ofHours(1));

        ArgumentCaptor<CommunicationEvent> captor = ArgumentCaptor.forClass(CommunicationEvent.class);
        verify(communicationService).publish(captor.capture());
        CommunicationEvent event = captor.getValue();

        assertThat(event.type()).isEqualTo(CommunicationEventType.PASSWORD_RESET_REQUESTED);
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.actorUserId()).isEqualTo(user.getId());
        assertThat(event.recipientUserId()).isEqualTo(user.getId());
        assertThat(event.channels()).containsExactly(CommunicationChannel.EMAIL);
        assertThat(event.priority()).isEqualTo(CommunicationPriority.HIGH);
        assertThat(event.variables().get("resetUrl")).isEqualTo(resetUrl);
        assertThat(event.variables().get("expirationMinutes")).isEqualTo("60");
        assertThat(event.entityRef().entityType()).isEqualTo("PASSWORD_RESET_TOKEN");
        assertThat(event.entityRef().entityId()).isEqualTo(tokenId);
        assertThat(event.idempotencyKey())
                .isEqualTo("PASSWORD_RESET_REQUESTED:" + tokenId + ":" + user.getId());
    }
}
