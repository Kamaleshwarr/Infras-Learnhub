package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.RepositoryProvider;
import com.company.learninghub.projectknowledge.domain.RepositoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectLinkedRepositoryRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @NotNull RepositoryType repositoryType,
        @NotNull RepositoryProvider provider,
        @NotBlank @Size(max = 2000) String repositoryUrl,
        @Size(max = 200) String defaultBranch,
        Integer displayOrder,
        Boolean active
) {
}
