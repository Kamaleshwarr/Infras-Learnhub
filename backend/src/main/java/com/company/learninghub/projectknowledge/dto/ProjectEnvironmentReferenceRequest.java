package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.EnvironmentReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectEnvironmentReferenceRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull EnvironmentReferenceType referenceType,
        @NotBlank @Size(max = 2000) String url,
        @Size(max = 5000) String description,
        Integer displayOrder,
        Boolean active
) {
}
