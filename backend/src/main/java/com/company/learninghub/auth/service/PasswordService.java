package com.company.learninghub.auth.service;

import com.company.learninghub.auth.dto.ChangePasswordRequest;
import com.company.learninghub.auth.repository.PasswordResetTokenRepository;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.validation.PasswordPolicyValidator;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final Clock clock;

    public PasswordService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator
    ) {
        this(userRepository, passwordResetTokenRepository, passwordEncoder, passwordPolicyValidator, Clock.systemUTC());
    }

    PasswordService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.clock = clock;
    }

    @Transactional
    public void changePassword(AuthenticatedUser authenticatedUser, ChangePasswordRequest request) {
        validateConfirmation(request.newPassword(), request.confirmNewPassword());
        User user = findUser(authenticatedUser.getId());

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must differ from current password");
        }

        passwordPolicyValidator.validate(request.newPassword(), user.getEmail());
        updatePassword(user, request.newPassword(), false);
    }

    @Transactional
    public void updatePassword(User user, String newPassword, boolean requirePasswordChange) {
        passwordPolicyValidator.validate(newPassword, user.getEmail());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(requirePasswordChange);
        if (!requirePasswordChange) {
            user.setPasswordChangedAt(clock.instant());
            passwordResetTokenRepository.invalidateActiveTokensForUser(user.getId(), clock.instant());
        }
        userRepository.save(user);
    }

    @Transactional
    public void updatePasswordFromReset(User user, String newPassword) {
        passwordPolicyValidator.validate(newPassword, user.getEmail());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(clock.instant());
        userRepository.save(user);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException("Current password is incorrect"));
    }

    private void validateConfirmation(String newPassword, String confirmNewPassword) {
        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
    }
}
