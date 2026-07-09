package com.company.learninghub.projectknowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectEnvironmentRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 5000) String description,
        Integer displayOrder,
        Boolean active
) {
}
