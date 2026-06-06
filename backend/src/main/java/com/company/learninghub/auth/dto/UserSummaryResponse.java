package com.company.learninghub.auth.dto;

import java.util.Set;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email,
        Set<String> roles
) {
}

