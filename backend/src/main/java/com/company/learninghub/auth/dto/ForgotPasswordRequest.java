package com.company.learninghub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to initiate a password reset email")
public record ForgotPasswordRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        @Schema(description = "Account email address", example = "employee@company.com")
        String email
) {
}
