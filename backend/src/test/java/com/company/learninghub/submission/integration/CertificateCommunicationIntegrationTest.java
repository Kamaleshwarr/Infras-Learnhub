package com.company.learninghub.submission.integration;

import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import com.company.learninghub.communication.repository.CommunicationOutboxRepository;
import com.company.learninghub.communication.service.CommunicationOutboxProcessor;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.submission.repository.CertificateSubmissionRepository;
import com.company.learninghub.submission.service.CertificateSubmissionService;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import com.company.learninghub.auth.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class CertificateCommunicationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_certificate_communication")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.catalog.import.enabled", () -> "false");
        registry.add("app.communication.enabled", () -> "true");
        registry.add("app.communication.email.provider", () -> "log");
    }

    @Autowired
    private CertificateSubmissionService submissionService;

    @Autowired
    private LearningInitiativeRepository initiativeRepository;

    @Autowired
    private CertificateSubmissionRepository submissionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CommunicationOutboxRepository outboxRepository;

    @Autowired
    private CommunicationOutboxProcessor outboxProcessor;

    @Autowired
    private UserRepository userRepository;

    private User employee;
    private User admin;
    private LearningInitiative initiative;

    @BeforeEach
    void setUp() {
        employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        admin = userRepository.findByEmailIgnoreCase("admin@learninghub.local").orElseThrow();
        initiative = createActiveInitiative();
    }

    @Test
    @Transactional
    void submitApproveAndRejectPublishInAppAndQueueEmails() {
        var submitResponse = submissionService.submit(
                initiative.getId(),
                certificateFile(),
                "Completed exam",
                AuthenticatedUser.from(employee)
        );
        CertificateSubmission submission = submissionRepository.findById(submitResponse.id()).orElseThrow();

        assertThat(notificationRepository.findAll().stream()
                .anyMatch(notification -> notification.getUser().getId().equals(admin.getId())
                        && notification.getType() == NotificationType.CERTIFICATE_SUBMITTED
                        && notification.getMessage().contains(initiative.getTitle()))).isTrue();
        assertThat(outboxRepository.findAll().stream()
                .anyMatch(entry -> entry.getEventType().name().equals("CERTIFICATE_SUBMITTED")
                        && entry.getStatus() == CommunicationOutboxStatus.PENDING)).isTrue();

        submissionService.approve(submission.getId(), AuthenticatedUser.from(admin));

        assertThat(notificationRepository.findAll().stream()
                .anyMatch(notification -> notification.getUser().getId().equals(employee.getId())
                        && notification.getType() == NotificationType.CERTIFICATE_APPROVED)).isTrue();
        assertThat(outboxRepository.findAll().stream()
                .anyMatch(entry -> entry.getEventType().name().equals("CERTIFICATE_APPROVED"))).isTrue();

        CertificateSubmission rejectedCandidate = createSubmittedSubmission("Resubmit Initiative");
        submissionService.reject(
                rejectedCandidate.getId(),
                "Please upload a clearer scan",
                AuthenticatedUser.from(admin)
        );

        assertThat(notificationRepository.findAll().stream()
                .anyMatch(notification -> notification.getUser().getId().equals(employee.getId())
                        && notification.getType() == NotificationType.CERTIFICATE_REJECTED
                        && notification.getMessage().contains("clearer scan"))).isTrue();
        assertThat(outboxRepository.findAll().stream()
                .anyMatch(entry -> entry.getEventType().name().equals("CERTIFICATE_REJECTED"))).isTrue();

        outboxProcessor.processOutbox();

        assertThat(outboxRepository.findAll().stream()
                .allMatch(entry -> entry.getStatus() == CommunicationOutboxStatus.SENT)).isTrue();
    }

    private LearningInitiative createActiveInitiative() {
        Instant now = Instant.now();
        LearningInitiative created = initiativeRepository.save(new LearningInitiative(
                "Communication Test Initiative " + now.toEpochMilli(),
                "Description",
                "Reward",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(30, ChronoUnit.DAYS),
                InitiativeStatus.ACTIVE,
                admin
        ));
        return created;
    }

    private CertificateSubmission createSubmittedSubmission(String title) {
        LearningInitiative localInitiative = initiativeRepository.save(new LearningInitiative(
                title + " " + Instant.now().toEpochMilli(),
                "Description",
                "Reward",
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS),
                InitiativeStatus.ACTIVE,
                admin
        ));
        var response = submissionService.submit(
                localInitiative.getId(),
                certificateFile(),
                null,
                AuthenticatedUser.from(employee)
        );
        return submissionRepository.findById(response.id()).orElseThrow();
    }

    private MockMultipartFile certificateFile() {
        return new MockMultipartFile(
                "certificateFile",
                "certificate.pdf",
                "application/pdf",
                "certificate".getBytes()
        );
    }
}
