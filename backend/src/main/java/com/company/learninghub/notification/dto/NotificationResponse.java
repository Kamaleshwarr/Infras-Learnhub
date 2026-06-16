package com.company.learninghub.notification.dto;

import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        NotificationEntityType entityType,
        UUID entityId,
        String actionPath,
        boolean read,
        Instant readAtUtc,
        Instant createdAtUtc
) {
}
