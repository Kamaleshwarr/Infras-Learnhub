package com.company.learninghub.studymaterial.dto;

import com.company.learninghub.studymaterial.domain.MaterialSourceType;
import com.company.learninghub.studymaterial.domain.MaterialType;

import java.time.Instant;
import java.util.UUID;

public record StudyMaterialResponse(
        UUID id,
        UUID folderId,
        String folderName,
        String title,
        String description,
        MaterialType materialType,
        MaterialSourceType sourceType,
        String originalFilename,
        String contentType,
        Long fileSizeBytes,
        String externalUrl,
        long downloadCount,
        StudyMaterialUserResponse uploadedBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

