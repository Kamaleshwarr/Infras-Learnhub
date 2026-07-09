package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.EnvironmentReferenceType;

import java.time.Instant;
import java.util.UUID;

public record ProjectEnvironmentReferenceResponse(
        UUID id,
        UUID environmentId,
        String name,
        EnvironmentReferenceType referenceType,
        String url,
        String description,
        int displayOrder,
        boolean active,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}
