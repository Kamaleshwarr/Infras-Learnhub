package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID projectId,
        ProjectUserResponse user,
        ProjectRole projectRole,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

