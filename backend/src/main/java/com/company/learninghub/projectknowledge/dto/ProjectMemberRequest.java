package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectMemberRequest(
        @NotNull UUID userId,
        @NotNull ProjectRole projectRole
) {
}

