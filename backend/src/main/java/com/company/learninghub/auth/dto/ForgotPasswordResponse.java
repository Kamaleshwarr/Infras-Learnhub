package com.company.learninghub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic response for password reset requests")
public record ForgotPasswordResponse(
        @Schema(
                description = "Informational message returned regardless of whether the email exists",
                example = "If an account exists for that email, password reset instructions have been sent."
        )
        String message
) {
}
