package com.company.learninghub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to reset a password using an email token")
public record ResetPasswordRequest(
        @NotBlank
        @Schema(description = "Password reset token from the email link")
        String token,

        @NotBlank
        @Size(min = 8, max = 128)
        @Schema(description = "New password meeting policy requirements", example = "NewSecurePass1!")
        String newPassword,

        @NotBlank
        @Size(min = 8, max = 128)
        @Schema(description = "Confirmation of the new password", example = "NewSecurePass1!")
        String confirmNewPassword
) {
}
