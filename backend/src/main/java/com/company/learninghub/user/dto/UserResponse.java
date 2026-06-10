package com.company.learninghub.user.dto;

import com.company.learninghub.user.domain.RoleName;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email,
        RoleName role,
        boolean active,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

