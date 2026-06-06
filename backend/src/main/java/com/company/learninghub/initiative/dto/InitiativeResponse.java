package com.company.learninghub.initiative.dto;

import com.company.learninghub.initiative.domain.InitiativeStatus;

import java.time.Instant;
import java.util.UUID;

public record InitiativeResponse(
        UUID id,
        String title,
        String description,
        String rewardDescription,
        Instant startDateUtc,
        Instant expiryDateUtc,
        InitiativeStatus status,
        InitiativeCreatedByResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

