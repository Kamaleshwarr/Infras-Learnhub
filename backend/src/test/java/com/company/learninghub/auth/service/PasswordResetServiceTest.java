package com.company.learninghub.auth.service;

import com.company.learninghub.auth.config.PasswordResetProperties;
import com.company.learninghub.auth.domain.PasswordResetToken;
import com.company.learninghub.auth.dto.ForgotPasswordRequest;
import com.company.learninghub.auth.dto.ResetPasswordRequest;
import com.company.learninghub.auth.repository.PasswordResetTokenRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private EmailService emailService;

    private PasswordResetService passwordResetService;
    private User user;
    private Clock clock;

    @BeforeEach
    void setUp() {
        PasswordResetProperties properties = new PasswordResetProperties();
        properties.setExpiration(Duration.ofHours(1));
        properties.setFrontendResetUrl("http://localhost:5173/reset-password");
        clock = Clock.fixed(Instant.parse("2026-06-11T12:00:00Z"), ZoneOffset.UTC);
        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetTokenRepository,
                passwordService,
                emailService,
                properties,
                new SecureRandom(),
                clock
        );

        user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
    }

    @Test
    void requestPasswordResetCreatesTokenAndSendsEmailForActiveUser() {
        when(userRepository.findByEmailIgnoreCase("employee@example.com")).thenReturn(Optional.of(user));

        passwordResetService.requestPasswordReset(new ForgotPasswordRequest("employee@example.com"));

        verify(passwordResetTokenRepository).invalidateActiveTokensForUser(eq(user.getId()), any());
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isNotBlank();
        verify(emailService).sendPasswordResetEmail(
                eq("employee@example.com"),
                eq("Employee One"),
                org.mockito.ArgumentMatchers.contains("token="),
                eq(Duration.ofHours(1))
        );
    }

    @Test
    void requestPasswordResetDoesNothingForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(new ForgotPasswordRequest("missing@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any(), any());
    }

    @Test
    void resetPasswordConsumesActiveToken() {
        String rawToken = "reset-token-value";
        String tokenHash = PasswordResetService.hashToken(rawToken);
        PasswordResetToken resetToken = new PasswordResetToken(user, tokenHash, Instant.parse("2026-06-11T13:00:00Z"));
        ResetPasswordRequest request = new ResetPasswordRequest(rawToken, "NewSecure1!", "NewSecure1!");

        when(passwordResetTokenRepository.findActiveToken(tokenHash, clock.instant()))
                .thenReturn(Optional.of(resetToken));

        passwordResetService.resetPassword(request);

        verify(passwordService).updatePasswordFromReset(user, "NewSecure1!");
        assertThat(resetToken.isUsed()).isTrue();
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        String rawToken = "expired-token";
        String tokenHash = PasswordResetService.hashToken(rawToken);
        when(passwordResetTokenRepository.findActiveToken(tokenHash, clock.instant())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword(
                new ResetPasswordRequest(rawToken, "NewSecure1!", "NewSecure1!")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired password reset token");
    }
}
