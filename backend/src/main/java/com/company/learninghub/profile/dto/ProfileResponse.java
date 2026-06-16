package com.company.learninghub.profile.dto;

import com.company.learninghub.user.domain.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Authenticated user's profile")
public record ProfileResponse(
        @Schema(description = "User identifier")
        UUID id,
        @Schema(description = "Employee identifier")
        String employeeId,
        @Schema(description = "Full name")
        String fullName,
        @Schema(description = "Email address")
        String email,
        @Schema(description = "Primary assigned role")
        RoleName role,
        @Schema(description = "Whether the account is active")
        boolean active,
        @Schema(description = "Whether the user must change password before accessing the application")
        boolean mustChangePassword,
        @Schema(description = "Whether the user has an uploaded avatar")
        boolean hasAvatar,
        @Schema(description = "Relative URL to fetch the avatar image when hasAvatar is true")
        String avatarUrl,
        @Schema(description = "Account creation timestamp (UTC)")
        Instant createdAtUtc,
        @Schema(description = "Last profile update timestamp (UTC)")
        Instant updatedAtUtc
) {
}
