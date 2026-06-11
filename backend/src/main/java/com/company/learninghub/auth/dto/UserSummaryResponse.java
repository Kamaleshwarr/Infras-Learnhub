package com.company.learninghub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Authenticated user summary")
public record UserSummaryResponse(
        @Schema(description = "User identifier")
        UUID id,
        @Schema(description = "Employee identifier")
        String employeeId,
        @Schema(description = "Full name")
        String fullName,
        @Schema(description = "Email address")
        String email,
        @Schema(description = "Assigned roles")
        Set<String> roles,
        @Schema(description = "Whether the user must change password before accessing the application")
        boolean mustChangePassword
) {
}

