package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.ProjectAccessType;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectAccessType accessType,
        boolean archived,
        ProjectUserResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

