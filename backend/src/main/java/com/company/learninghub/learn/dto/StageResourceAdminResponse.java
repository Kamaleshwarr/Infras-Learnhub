package com.company.learninghub.learn.dto;

public record StageResourceAdminResponse(
        String stageSlug,
        String stageTitle,
        int stageOrder,
        java.util.List<ManagedResourceResponse> resources
) {
}
