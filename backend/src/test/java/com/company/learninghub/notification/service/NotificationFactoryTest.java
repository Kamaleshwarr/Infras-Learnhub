package com.company.learninghub.notification.service;

import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.notification.domain.Notification;
import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.submission.domain.CertificateDocument;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationFactoryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-06-16T12:00:00Z");

    private NotificationFactory notificationFactory;

    @BeforeEach
    void setUp() {
        notificationFactory = new NotificationFactory();
    }

    @Test
    void certificateSubmittedIncludesEmployeeAndInitiativeDetails() {
        User admin = adminUser();
        CertificateSubmission submission = submission();

        Notification notification = notificationFactory.certificateSubmitted(admin, submission, CREATED_AT);

        assertThat(notification.getType()).isEqualTo(NotificationType.CERTIFICATE_SUBMITTED);
        assertThat(notification.getUser()).isEqualTo(admin);
        assertThat(notification.getMessage()).contains("Jane Doe");
        assertThat(notification.getMessage()).contains("Cloud Fundamentals");
        assertThat(notification.getEntityType()).isEqualTo(NotificationEntityType.CERTIFICATE_SUBMISSION);
        assertThat(notification.getActionPath()).isEqualTo("/submissions/review");
    }

    @Test
    void certificateRejectedIncludesReasonWhenPresent() {
        User employee = employeeUser();
        CertificateSubmission submission = submission();
        submission.reject(adminUser(), CREATED_AT, "Document is unreadable");

        Notification notification = notificationFactory.certificateRejected(employee, submission, CREATED_AT);

        assertThat(notification.getType()).isEqualTo(NotificationType.CERTIFICATE_REJECTED);
        assertThat(notification.getMessage()).contains("Document is unreadable");
    }

    @Test
    void accountCreatedPointsToChangePassword() {
        User user = employeeUser();

        Notification notification = notificationFactory.accountCreated(user, CREATED_AT);

        assertThat(notification.getType()).isEqualTo(NotificationType.ACCOUNT_CREATED);
        assertThat(notification.getActionPath()).isEqualTo("/change-password");
        assertThat(notification.getEntityType()).isEqualTo(NotificationEntityType.USER);
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }

    private User adminUser() {
        User user = new User("ADMIN001", "admin@company.com", "Admin User", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.ADMIN));
        return user;
    }

    private CertificateSubmission submission() {
        User employee = employeeUser();
        User creator = adminUser();
        LearningInitiative initiative = new LearningInitiative(
                "Cloud Fundamentals",
                "Description",
                "Reward",
                CREATED_AT.minusSeconds(3600),
                CREATED_AT.plusSeconds(3600),
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
                CREATED_AT.minusSeconds(60)
        );
        ReflectionTestUtils.setField(submission, "id", UUID.randomUUID());
        return submission;
    }
}
