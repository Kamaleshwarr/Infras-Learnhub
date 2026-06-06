package com.company.learninghub.projectknowledge.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectFolderResponse(
        UUID id,
        UUID projectId,
        String name,
        String description,
        UUID parentId,
        ProjectUserResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

