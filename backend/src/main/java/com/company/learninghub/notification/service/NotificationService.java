package com.company.learninghub.notification.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.notification.domain.Notification;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.notification.dto.NotificationResponse;
import com.company.learninghub.notification.dto.UnreadCountResponse;
import com.company.learninghub.notification.mapper.NotificationMapper;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * In-app notification inbox service.
 * <p>
 * Certificate workflow notifications are produced by the Communication Framework (C3).
 * Account lifecycle helper methods remain for schema compatibility and a future email workstream;
 * they are not called by domain producers in v0.6.
 */
@Service
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationMapper notificationMapper;
    private final Clock clock;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationFactory notificationFactory,
            NotificationMapper notificationMapper
    ) {
        this(
                notificationRepository,
                userRepository,
                notificationFactory,
                notificationMapper,
                Clock.systemUTC()
        );
    }

    NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationFactory notificationFactory,
            NotificationMapper notificationMapper,
            Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationFactory = notificationFactory;
        this.notificationMapper = notificationMapper;
        this.clock = clock;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(
            Boolean read,
            NotificationType type,
            Pageable pageable,
            AuthenticatedUser authenticatedUser
    ) {
        UUID userId = authenticatedUser.getId();
        Specification<Notification> specification = Specification
                .where(belongsToUser(userId))
                .and(hasReadState(read))
                .and(hasType(type));
        return notificationRepository.findAll(specification, normalizePageable(pageable))
                .map(notificationMapper::toResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(AuthenticatedUser authenticatedUser) {
        long count = notificationRepository.countByUserIdAndReadAtIsNull(authenticatedUser.getId());
        return new UnreadCountResponse(count);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public NotificationResponse markRead(UUID notificationId, AuthenticatedUser authenticatedUser) {
        Notification notification = findOwnedNotificationOrThrow(notificationId, authenticatedUser.getId());
        notification.markRead(Instant.now(clock));
        return notificationMapper.toResponse(notification);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void markAllRead(AuthenticatedUser authenticatedUser) {
        notificationRepository.markAllReadForUser(authenticatedUser.getId(), Instant.now(clock));
    }

    @Transactional
    public void notifyCertificateSubmitted(CertificateSubmission submission) {
        Instant createdAt = Instant.now(clock);
        List<User> admins = userRepository.findActiveByRoleName(RoleName.ADMIN);
        if (admins.isEmpty()) {
            return;
        }
        List<Notification> notifications = admins.stream()
                .map(admin -> notificationFactory.certificateSubmitted(admin, submission, createdAt))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void notifyCertificateApproved(CertificateSubmission submission) {
        saveNotification(notificationFactory.certificateApproved(
                submission.getEmployee(),
                submission,
                Instant.now(clock)
        ));
    }

    @Transactional
    public void notifyCertificateRejected(CertificateSubmission submission) {
        saveNotification(notificationFactory.certificateRejected(
                submission.getEmployee(),
                submission,
                Instant.now(clock)
        ));
    }

    @Transactional
    public void notifyPasswordResetByAdmin(User user) {
        saveNotification(notificationFactory.passwordResetByAdmin(user, Instant.now(clock)));
    }

    @Transactional
    public void notifyAccountActivated(User user) {
        saveNotification(notificationFactory.accountActivated(user, Instant.now(clock)));
    }

    @Transactional
    public void notifyAccountDeactivated(User user) {
        saveNotification(notificationFactory.accountDeactivated(user, Instant.now(clock)));
    }

    @Transactional
    public void notifyAccountCreated(User user) {
        saveNotification(notificationFactory.accountCreated(user, Instant.now(clock)));
    }

    private void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    private Notification findOwnedNotificationOrThrow(UUID notificationId, UUID userId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification was not found"));
    }

    private Specification<Notification> belongsToUser(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    private Specification<Notification> hasReadState(Boolean read) {
        return (root, query, criteriaBuilder) -> {
            if (read == null) {
                return criteriaBuilder.conjunction();
            }
            return read
                    ? criteriaBuilder.isNotNull(root.get("readAt"))
                    : criteriaBuilder.isNull(root.get("readAt"));
        };
    }

    private Specification<Notification> hasType(NotificationType type) {
        return (root, query, criteriaBuilder) -> type == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("type"), type);
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(this::toRepositorySortOrder)
                .toList());
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }

    private Sort.Order toRepositorySortOrder(Sort.Order order) {
        String property = switch (order.getProperty()) {
            case "createdAt", "createdAtUtc" -> "createdAt";
            case "readAt", "readAtUtc" -> "readAt";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        };
        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}
