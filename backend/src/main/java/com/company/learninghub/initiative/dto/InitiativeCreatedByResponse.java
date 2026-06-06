package com.company.learninghub.initiative.dto;

import java.util.UUID;

public record InitiativeCreatedByResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email
) {
}

