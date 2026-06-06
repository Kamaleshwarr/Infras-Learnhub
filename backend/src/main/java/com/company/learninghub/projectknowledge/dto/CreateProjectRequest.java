package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @NotNull ProjectAccessType accessType
) {
}

