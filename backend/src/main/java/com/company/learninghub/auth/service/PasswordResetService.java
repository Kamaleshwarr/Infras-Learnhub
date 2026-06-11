package com.company.learninghub.auth.service;

import com.company.learninghub.auth.config.PasswordResetProperties;
import com.company.learninghub.auth.domain.PasswordResetToken;
import com.company.learninghub.auth.dto.ForgotPasswordRequest;
import com.company.learninghub.auth.dto.ResetPasswordRequest;
import com.company.learninghub.auth.repository.PasswordResetTokenRepository;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

@Service
public class PasswordResetService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final PasswordResetProperties passwordResetProperties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordService passwordService,
            EmailService emailService,
            PasswordResetProperties passwordResetProperties
    ) {
        this(
                userRepository,
                passwordResetTokenRepository,
                passwordService,
                emailService,
                passwordResetProperties,
                new SecureRandom(),
                Clock.systemUTC()
        );
    }

    PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordService passwordService,
            EmailService emailService,
            PasswordResetProperties passwordResetProperties,
            SecureRandom secureRandom,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordService = passwordService;
        this.emailService = emailService;
        this.passwordResetProperties = passwordResetProperties;
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(User::isActive)
                .ifPresent(this::issueResetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validateConfirmation(request.newPassword(), request.confirmNewPassword());

        Instant now = clock.instant();
        String tokenHash = hashToken(request.token());
        PasswordResetToken resetToken = passwordResetTokenRepository.findActiveToken(tokenHash, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        User user = resetToken.getUser();
        passwordService.updatePasswordFromReset(user, request.newPassword());
        resetToken.markUsed(now);
        passwordResetTokenRepository.invalidateActiveTokensForUser(user.getId(), now);
    }

    private void issueResetToken(User user) {
        Instant now = clock.instant();
        passwordResetTokenRepository.invalidateActiveTokensForUser(user.getId(), now);

        String rawToken = generateToken();
        Instant expiresAt = now.plus(passwordResetProperties.getExpiration());
        PasswordResetToken resetToken = new PasswordResetToken(user, hashToken(rawToken), expiresAt);
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = buildResetUrl(rawToken);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl, passwordResetProperties.getExpiration());
    }

    private String buildResetUrl(String rawToken) {
        String baseUrl = passwordResetProperties.getFrontendResetUrl();
        if (baseUrl.contains("?")) {
            return baseUrl + "&token=" + rawToken;
        }
        return baseUrl + "?token=" + rawToken;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private void validateConfirmation(String newPassword, String confirmNewPassword) {
        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
    }
}
