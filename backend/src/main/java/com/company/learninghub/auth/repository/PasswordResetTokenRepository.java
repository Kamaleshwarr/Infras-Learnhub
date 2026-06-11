package com.company.learninghub.auth.repository;

import com.company.learninghub.auth.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);

    @Modifying
    @Query("""
            UPDATE PasswordResetToken token
            SET token.usedAt = :usedAt
            WHERE token.user.id = :userId
              AND token.usedAt IS NULL
            """)
    int invalidateActiveTokensForUser(@Param("userId") UUID userId, @Param("usedAt") Instant usedAt);

    @Query("""
            SELECT token
            FROM PasswordResetToken token
            WHERE token.tokenHash = :tokenHash
              AND token.usedAt IS NULL
              AND token.expiresAt > :now
            """)
    Optional<PasswordResetToken> findActiveToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);
}
