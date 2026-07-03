package com.company.learninghub.learn.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEnrollmentRequest(
        @NotNull UUID technologyId
) {
}
