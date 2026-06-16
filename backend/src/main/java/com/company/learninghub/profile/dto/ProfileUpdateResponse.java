package com.company.learninghub.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profile update result with optional new access token when email changes")
public record ProfileUpdateResponse(
        @Schema(description = "Updated profile")
        ProfileResponse profile,
        @Schema(description = "New JWT when the email address changed; omitted when unchanged")
        String accessToken
) {
}
