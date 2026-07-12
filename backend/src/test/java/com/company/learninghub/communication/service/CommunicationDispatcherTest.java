package com.company.learninghub.communication.service;

import com.company.learninghub.communication.channel.EmailChannelHandler;
import com.company.learninghub.communication.channel.InAppChannelHandler;
import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunicationDispatcherTest {

    @Mock
    private InAppChannelHandler inAppChannelHandler;
    @Mock
    private EmailChannelHandler emailChannelHandler;
    @Mock
    private UserRepository userRepository;

    private CommunicationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new CommunicationDispatcher(inAppChannelHandler, emailChannelHandler, userRepository);
    }

    @Test
    void dispatchRoutesToInAppAndEmailChannels() {
        User recipient = new User("E001", "employee@learninghub.local", "Jane Doe", "hash");
        recipient.setActive(true);
        CommunicationEvent event = event(Set.of(CommunicationChannel.IN_APP, CommunicationChannel.EMAIL));
        when(userRepository.findById(event.recipientUserId())).thenReturn(Optional.of(recipient));

        dispatcher.dispatch(event);

        verify(inAppChannelHandler).deliver(eq(event), eq(recipient));
        verify(emailChannelHandler).enqueue(event);
    }

    @Test
    void dispatchSkipsInactiveRecipient() {
        User recipient = new User("E001", "employee@learninghub.local", "Jane Doe", "hash");
        recipient.setActive(false);
        CommunicationEvent event = event(Set.of(CommunicationChannel.IN_APP, CommunicationChannel.EMAIL));
        when(userRepository.findById(event.recipientUserId())).thenReturn(Optional.of(recipient));

        dispatcher.dispatch(event);

        verify(inAppChannelHandler, never()).deliver(any(), any());
        verify(emailChannelHandler, never()).enqueue(any());
    }

    private CommunicationEvent event(Set<CommunicationChannel> channels) {
        UUID recipientId = UUID.randomUUID();
        return new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.CERTIFICATE_APPROVED,
                Instant.parse("2026-07-12T12:00:00Z"),
                UUID.randomUUID(),
                recipientId,
                new CommunicationEntityRef("CERTIFICATE_SUBMISSION", UUID.randomUUID(), "/submissions"),
                Map.of("title", "Certificate approved", "message", "Approved"),
                channels,
                CommunicationPriority.NORMAL,
                "CERTIFICATE_APPROVED:1:" + recipientId
        );
    }
}
