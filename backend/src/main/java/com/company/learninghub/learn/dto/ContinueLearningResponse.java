package com.company.learninghub.learn.dto;

public record ContinueLearningResponse(
        String enrollmentId,
        String technologyId,
        String technologySlug,
        String technologyName,
        String currentStageId,
        int currentStageOrder,
        String currentStageTitle,
        int progressPercent
) {
}
