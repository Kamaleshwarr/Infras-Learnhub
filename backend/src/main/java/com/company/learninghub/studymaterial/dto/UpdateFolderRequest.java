package com.company.learninghub.studymaterial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateFolderRequest(
        @NotBlank
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        UUID parentId
) {
}

