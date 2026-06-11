package com.company.learninghub.auth.service;

import com.company.learninghub.auth.dto.ChangePasswordRequest;
import com.company.learninghub.auth.repository.PasswordResetTokenRepository;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.validation.PasswordPolicyValidator;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordService passwordService;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        PasswordPolicyValidator passwordPolicyValidator = new PasswordPolicyValidator();
        Clock clock = Clock.fixed(Instant.parse("2026-06-11T12:00:00Z"), ZoneOffset.UTC);
        passwordService = new PasswordService(
                userRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                passwordPolicyValidator,
                clock
        );

        user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$current");
        userId = UUID.randomUUID();
        setField(user, "id", userId);
        user.assignRole(new Role(RoleName.EMPLOYEE));
    }

    @Test
    void changePasswordUpdatesHashAndClearsMustChangeFlag() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);
        ChangePasswordRequest request = new ChangePasswordRequest("CurrentPass1!", "NewSecure1!", "NewSecure1!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass1!", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("NewSecure1!", user.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("NewSecure1!")).thenReturn("$2a$12$new");
        when(userRepository.save(user)).thenReturn(user);

        passwordService.changePassword(authenticatedUser, request);

        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getPasswordChangedAt()).isEqualTo(Instant.parse("2026-06-11T12:00:00Z"));
        verify(passwordResetTokenRepository).invalidateActiveTokensForUser(eq(userId), any());
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);
        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass1!", "NewSecure1!", "NewSecure1!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1!", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> passwordService.changePassword(authenticatedUser, request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Current password is incorrect");
    }

    @Test
    void changePasswordRejectsSamePassword() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);
        ChangePasswordRequest request = new ChangePasswordRequest("CurrentPass1!", "CurrentPass1!", "CurrentPass1!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass1!", user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> passwordService.changePassword(authenticatedUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must differ from current password");
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
