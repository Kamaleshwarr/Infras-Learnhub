package com.company.learninghub.learn.dto;

public record EnrollmentResponse(
        String id,
        String technologyId,
        String technologySlug,
        String technologyName,
        String status,
        String enrolledAt,
        String startedAt,
        String lastActivityAt,
        String completedAt,
        int progressPercent,
        String currentStageId,
        Integer currentStageOrder,
        String currentStageTitle,
        String nextStageId,
        Integer nextStageOrder,
        String nextStageTitle
) {
}
