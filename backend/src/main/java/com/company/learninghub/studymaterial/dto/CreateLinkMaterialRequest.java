package com.company.learninghub.studymaterial.dto;

import com.company.learninghub.studymaterial.domain.MaterialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLinkMaterialRequest(
        UUID folderId,

        @NotBlank
        @Size(max = 250)
        String title,

        @Size(max = 5000)
        String description,

        @NotNull
        MaterialType materialType,

        @NotBlank
        @Size(max = 2000)
        String externalUrl
) {
}

