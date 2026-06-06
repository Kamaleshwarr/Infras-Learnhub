package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProjectLinkRequest(
        UUID folderId,
        @NotBlank @Size(max = 250) String title,
        @Size(max = 5000) String description,
        @NotNull KnowledgeCategory category,
        @NotBlank @Size(max = 2000) String externalUrl
) {
}

