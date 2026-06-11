package com.company.learninghub.auth.controller;

import com.company.learninghub.auth.dto.ChangePasswordRequest;
import com.company.learninghub.auth.dto.ForgotPasswordRequest;
import com.company.learninghub.auth.dto.ForgotPasswordResponse;
import com.company.learninghub.auth.dto.LoginRequest;
import com.company.learninghub.auth.dto.LoginResponse;
import com.company.learninghub.auth.dto.ResetPasswordRequest;
import com.company.learninghub.auth.dto.UserSummaryResponse;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.service.AuthenticationService;
import com.company.learninghub.auth.service.PasswordResetService;
import com.company.learninghub.auth.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "JWT based authentication APIs")
public class AuthenticationController {

    private static final String FORGOT_PASSWORD_MESSAGE =
            "If an account exists for that email, password reset instructions have been sent.";

    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;
    private final PasswordResetService passwordResetService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            PasswordService passwordService,
            PasswordResetService passwordResetService
    ) {
        this.authenticationService = authenticationService;
        this.passwordService = passwordService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and issue a JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Return the current authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserSummaryResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(authenticationService.toUserSummary(user));
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change the authenticated user's password",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password changed successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation or policy failure",
                            content = @Content(schema = @Schema(implementation = com.company.learninghub.common.exception.ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Current password is incorrect",
                            content = @Content(schema = @Schema(implementation = com.company.learninghub.common.exception.ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        passwordService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request a password reset email",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Request accepted",
                            content = @Content(schema = @Schema(implementation = ForgotPasswordResponse.class)))
            }
    )
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ForgotPasswordResponse(FORGOT_PASSWORD_MESSAGE));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset a password using an email token",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password reset successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid token or validation failure",
                            content = @Content(schema = @Schema(implementation = com.company.learninghub.common.exception.ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}

