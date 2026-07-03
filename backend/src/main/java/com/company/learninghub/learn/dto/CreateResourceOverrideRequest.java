package com.company.learninghub.learn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateResourceOverrideRequest(
        @NotBlank @Size(max = 100) String stageSlug,
        @Size(max = 100) String catalogResourceSlug,
        @NotBlank @Size(max = 100) String resourceSlug,
        @NotBlank String resourceKind,
        Boolean disabled,
        @Size(max = 2048) String overrideUrl,
        Boolean preferred,
        Boolean enabled,
        @Size(max = 2000) String reason,
        @Size(max = 200) String title,
        String resourceType,
        @Size(max = 100) String provider,
        String freePaid,
        Integer resourceOrder
) {
}
