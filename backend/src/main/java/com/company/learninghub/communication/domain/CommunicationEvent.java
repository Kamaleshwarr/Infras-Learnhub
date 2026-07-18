package com.company.learninghub.communication.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CommunicationEvent(
        UUID eventId,
        CommunicationEventType type,
        Instant occurredAt,
        UUID actorUserId,
        UUID recipientUserId,
        CommunicationEntityRef entityRef,
        Map<String, String> variables,
        Set<CommunicationChannel> channels,
        CommunicationPriority priority,
        String idempotencyKey
) {
    public CommunicationEvent {
        variables = variables == null ? Map.of() : Map.copyOf(variables);
        channels = channels == null ? Set.of() : Set.copyOf(channels);
        if (priority == null) {
            priority = CommunicationPriority.NORMAL;
        }
    }
}
