package com.company.learninghub.auth.communication;

import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.service.CommunicationService;
import com.company.learninghub.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Publishes password-reset communication events through the Communication Framework.
 * Channel selection and template mapping remain encapsulated here so auth services stay channel-agnostic.
 */
@Component
public class PasswordResetCommunicationPublisher {

    private static final Set<CommunicationChannel> EMAIL_ONLY = Set.of(CommunicationChannel.EMAIL);

    private final CommunicationService communicationService;
    private final Clock clock;

    @Autowired
    public PasswordResetCommunicationPublisher(CommunicationService communicationService) {
        this(communicationService, Clock.systemUTC());
    }

    PasswordResetCommunicationPublisher(CommunicationService communicationService, Clock clock) {
        this.communicationService = communicationService;
        this.clock = clock;
    }

    public void publishPasswordResetRequested(User user, UUID tokenId, String resetUrl, Duration expiration) {
        CommunicationEvent event = new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.PASSWORD_RESET_REQUESTED,
                Instant.now(clock),
                user.getId(),
                user.getId(),
                new CommunicationEntityRef("PASSWORD_RESET_TOKEN", tokenId, "/reset-password"),
                Map.of(
                        "resetUrl", resetUrl,
                        "expirationMinutes", String.valueOf(expiration.toMinutes())
                ),
                EMAIL_ONLY,
                CommunicationPriority.HIGH,
                idempotencyKey(tokenId, user.getId())
        );
        communicationService.publish(event);
    }

    private String idempotencyKey(UUID tokenId, UUID recipientUserId) {
        return CommunicationEventType.PASSWORD_RESET_REQUESTED.name() + ":" + tokenId + ":" + recipientUserId;
    }
}
