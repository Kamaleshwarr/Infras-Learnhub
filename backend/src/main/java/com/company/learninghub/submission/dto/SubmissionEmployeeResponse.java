package com.company.learninghub.submission.dto;

import java.util.UUID;

public record SubmissionEmployeeResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email
) {
}

