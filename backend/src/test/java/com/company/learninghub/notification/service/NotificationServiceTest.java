package com.company.learninghub.notification.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.notification.domain.Notification;
import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.notification.dto.NotificationResponse;
import com.company.learninghub.notification.dto.UnreadCountResponse;
import com.company.learninghub.notification.mapper.NotificationMapper;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.domain.CertificateDocument;
import com.company.learninghub.submission.domain.CertificateSubmission;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-06-16T12:00:00Z");

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationFactory notificationFactory;
    private NotificationMapper notificationMapper;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationFactory = new NotificationFactory();
        notificationMapper = new NotificationMapper();
        notificationService = new NotificationService(
                notificationRepository,
                userRepository,
                notificationFactory,
                notificationMapper,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void listReturnsMappedNotificationsForCurrentUser() {
        User user = employeeUser();
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        Notification notification = unreadNotification(user);
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<NotificationResponse> response = notificationService.list(false, null, PageRequest.of(0, 20), principal);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().read()).isFalse();
        assertThat(response.getContent().getFirst().type()).isEqualTo(NotificationType.ACCOUNT_CREATED);
    }

    @Test
    void unreadCountReturnsRepositoryCount() {
        User user = employeeUser();
        when(notificationRepository.countByUserIdAndReadAtIsNull(user.getId())).thenReturn(4L);

        UnreadCountResponse response = notificationService.unreadCount(AuthenticatedUser.from(user));

        assertThat(response.count()).isEqualTo(4L);
    }

    @Test
    void markReadSetsReadAtWhenUnread() {
        User user = employeeUser();
        Notification notification = unreadNotification(user);
        when(notificationRepository.findByIdAndUserId(notification.getId(), user.getId()))
                .thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markRead(notification.getId(), AuthenticatedUser.from(user));

        assertThat(notification.getReadAt()).isEqualTo(FIXED_NOW);
        assertThat(response.read()).isTrue();
        assertThat(response.readAtUtc()).isEqualTo(FIXED_NOW);
    }

    @Test
    void markReadIsIdempotentWhenAlreadyRead() {
        User user = employeeUser();
        Notification notification = unreadNotification(user);
        Instant existingReadAt = Instant.parse("2026-06-15T10:00:00Z");
        notification.markRead(existingReadAt);
        when(notificationRepository.findByIdAndUserId(notification.getId(), user.getId()))
                .thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markRead(notification.getId(), AuthenticatedUser.from(user));

        assertThat(notification.getReadAt()).isEqualTo(existingReadAt);
        assertThat(response.readAtUtc()).isEqualTo(existingReadAt);
    }

    @Test
    void markReadThrowsWhenNotificationNotOwned() {
        User user = employeeUser();
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndUserId(notificationId, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(notificationId, AuthenticatedUser.from(user)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification was not found");
    }

    @Test
    void markAllReadDelegatesToRepository() {
        User user = employeeUser();

        notificationService.markAllRead(AuthenticatedUser.from(user));

        verify(notificationRepository).markAllReadForUser(user.getId(), FIXED_NOW);
    }

    @Test
    void notifyCertificateSubmittedCreatesNotificationForEachActiveAdmin() {
        User adminOne = adminUser("ADMIN001", "admin.one@company.com");
        User adminTwo = adminUser("ADMIN002", "admin.two@company.com");
        CertificateSubmission submission = submission(employeeUser());
        when(userRepository.findActiveByRoleName(RoleName.ADMIN)).thenReturn(List.of(adminOne, adminTwo));

        notificationService.notifyCertificateSubmitted(submission);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(notification -> notification.getType() == NotificationType.CERTIFICATE_SUBMITTED);
        assertThat(saved).extracting(notification -> notification.getUser().getId())
                .containsExactlyInAnyOrder(adminOne.getId(), adminTwo.getId());
    }

    @Test
    void notifyCertificateApprovedPersistsEmployeeNotification() {
        User employee = employeeUser();
        CertificateSubmission submission = submission(employee);
        submission.approve(adminUser("ADMIN001", "admin@company.com"), FIXED_NOW);

        notificationService.notifyCertificateApproved(submission);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.CERTIFICATE_APPROVED);
        assertThat(saved.getUser().getId()).isEqualTo(employee.getId());
        assertThat(saved.getActionPath()).isEqualTo("/submissions");
    }

    @Test
    void notifyAccountCreatedPersistsWelcomeNotification() {
        User user = employeeUser();

        notificationService.notifyAccountCreated(user);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.ACCOUNT_CREATED);
        assertThat(saved.getTitle()).isEqualTo("Welcome to Learning Hub");
        assertThat(saved.getActionPath()).isEqualTo("/change-password");
    }

    private Notification unreadNotification(User user) {
        Notification notification = new Notification(
                user,
                NotificationType.ACCOUNT_CREATED,
                "Welcome",
                "Welcome message",
                NotificationEntityType.USER,
                user.getId(),
                "/change-password",
                FIXED_NOW
        );
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        return notification;
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }

    private User adminUser(String employeeId, String email) {
        User user = new User(employeeId, email, "Admin User", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.ADMIN));
        return user;
    }

    private CertificateSubmission submission(User employee) {
        User creator = adminUser("ADMIN001", "admin@company.com");
        LearningInitiative initiative = new LearningInitiative(
                "Cloud Fundamentals",
                "Description",
                "Reward",
                FIXED_NOW.minusSeconds(3600),
                FIXED_NOW.plusSeconds(3600),
                InitiativeStatus.ACTIVE,
                creator
        );
        ReflectionTestUtils.setField(initiative, "id", UUID.randomUUID());
        CertificateDocument document = new CertificateDocument(
                "local",
                "certificates/key.pdf",
                "certificate.pdf",
                "application/pdf",
                1024L,
                employee
        );
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());
        CertificateSubmission submission = new CertificateSubmission(
                employee,
                initiative,
                document,
                "Completed course",
                FIXED_NOW.minusSeconds(60)
        );
        ReflectionTestUtils.setField(submission, "id", UUID.randomUUID());
        return submission;
    }
}
