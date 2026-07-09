package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.RepositoryProvider;
import com.company.learninghub.projectknowledge.domain.RepositoryType;

import java.time.Instant;
import java.util.UUID;

public record ProjectLinkedRepositoryResponse(
        UUID id,
        UUID projectId,
        String name,
        String description,
        RepositoryType repositoryType,
        RepositoryProvider provider,
        String repositoryUrl,
        String defaultBranch,
        int displayOrder,
        boolean active,
        ProjectUserResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}
