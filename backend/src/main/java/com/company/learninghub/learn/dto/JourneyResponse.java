package com.company.learninghub.learn.dto;

import java.util.List;

public record JourneyResponse(
        ContinueLearningResponse continueLearning,
        List<EnrollmentResponse> active,
        List<EnrollmentResponse> completed,
        List<EnrollmentResponse> left
) {
}
