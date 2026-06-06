package com.company.learninghub.studymaterial.dto;

import java.time.Instant;
import java.util.UUID;

public record StudyMaterialFolderResponse(
        UUID id,
        String name,
        String description,
        UUID parentId,
        StudyMaterialUserResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

