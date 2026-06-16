package com.company.learninghub.notification.repository;

import com.company.learninghub.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndReadAtIsNull(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notification notification
            SET notification.readAt = :readAt
            WHERE notification.user.id = :userId
              AND notification.readAt IS NULL
            """)
    int markAllReadForUser(@Param("userId") UUID userId, @Param("readAt") Instant readAt);
}
