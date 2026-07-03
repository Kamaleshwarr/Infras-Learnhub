package com.company.learninghub.learn.dto;

public record ManagedResourceResponse(
        RoadmapResourceResponse catalog,
        RoadmapResourceResponse effective,
        ResourceOverrideResponse override,
        String overrideStatus
) {
}
