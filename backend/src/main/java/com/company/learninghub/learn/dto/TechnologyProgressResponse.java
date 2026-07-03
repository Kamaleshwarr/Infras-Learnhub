package com.company.learninghub.learn.dto;

import java.util.List;

public record TechnologyProgressResponse(
        String enrollmentId,
        String technologyId,
        String technologySlug,
        String technologyName,
        String status,
        String enrolledAt,
        String startedAt,
        String lastActivityAt,
        String completedAt,
        int progressPercent,
        int totalStages,
        int completedStageCount,
        String currentStageId,
        Integer currentStageOrder,
        String currentStageTitle,
        String nextStageId,
        Integer nextStageOrder,
        String nextStageTitle,
        String estimatedRemainingEffort,
        List<String> completedStageIds,
        List<Integer> completedStageOrders
) {
}
