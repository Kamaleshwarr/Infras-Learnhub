package com.company.learninghub.studymaterial.dto;

import java.util.UUID;

public record StudyMaterialUserResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email
) {
}

