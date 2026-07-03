package com.company.learninghub.learn.dto;

import java.util.List;

public record RoadmapResponse(
        String technologyId,
        String technologySlug,
        String technologyName,
        String version,
        String description,
        String source,
        String sourceUrl,
        String catalogUpdatedAt,
        int stageCount,
        String estimatedTotalEffort,
        int recommendedStageOrder,
        int nextStageOrder,
        List<RoadmapStageResponse> stages
) {
}
