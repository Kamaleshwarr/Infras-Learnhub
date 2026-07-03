package com.company.learninghub.learn.dto;

import java.util.UUID;

public record TechnologyCreatedByResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email
) {
}
