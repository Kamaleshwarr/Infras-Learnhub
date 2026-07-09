package com.company.learninghub.projectknowledge.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectEnvironmentResponse(
        UUID id,
        UUID projectId,
        String name,
        String description,
        int displayOrder,
        boolean active,
        ProjectUserResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc,
        long referenceCount,
        List<ProjectEnvironmentReferenceResponse> references
) {
}
