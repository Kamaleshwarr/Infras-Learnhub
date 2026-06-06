package com.company.learninghub.studymaterial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateMaterialRequest(
        UUID folderId,

        @NotBlank
        @Size(max = 250)
        String title,

        @Size(max = 5000)
        String description,

        @Size(max = 2000)
        String externalUrl
) {
}

