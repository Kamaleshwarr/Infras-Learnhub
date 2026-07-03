package com.company.learninghub.learn.dto;

import java.util.List;

public record RoadmapStageResponse(
        int order,
        String slug,
        String title,
        String description,
        String estimatedEffort,
        String notes,
        List<RoadmapResourceResponse> learningResources,
        List<RoadmapResourceResponse> practiceResources
) {
}
