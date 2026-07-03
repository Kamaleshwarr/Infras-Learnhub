package com.company.learninghub.learn.dto;

public record ResourceOverrideResponse(
        String id,
        String technologySlug,
        String stageSlug,
        String resourceSlug,
        String catalogResourceSlug,
        String resourceKind,
        boolean disabled,
        String overrideUrl,
        boolean preferred,
        boolean enabled,
        String reason,
        String title,
        String resourceType,
        String provider,
        String freePaid,
        int resourceOrder,
        boolean organizationResource,
        String status
) {
}
