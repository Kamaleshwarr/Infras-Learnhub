package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import com.company.learninghub.projectknowledge.domain.KnowledgeSourceType;

import java.time.Instant;
import java.util.UUID;

public record ProjectKnowledgeItemResponse(
        UUID id,
        UUID projectId,
        UUID folderId,
        String folderName,
        String title,
        String description,
        KnowledgeCategory category,
        KnowledgeSourceType sourceType,
        String originalFilename,
        String contentType,
        Long fileSizeBytes,
        String externalUrl,
        long accessCount,
        ProjectUserResponse uploadedBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

