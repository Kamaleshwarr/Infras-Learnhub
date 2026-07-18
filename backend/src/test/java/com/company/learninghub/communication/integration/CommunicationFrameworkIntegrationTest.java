package com.company.learninghub.communication.integration;

import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.repository.CommunicationOutboxRepository;
import com.company.learninghub.communication.service.CommunicationOutboxProcessor;
import com.company.learninghub.communication.service.CommunicationService;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class CommunicationFrameworkIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_communication")
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
    private CommunicationService communicationService;

    @Autowired
    private CommunicationOutboxRepository outboxRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunicationOutboxProcessor outboxProcessor;

    private User employee;

    @BeforeEach
    void setUp() {
        employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
    }

    @Test
    void publishDeliversInAppAndProcessesEmailOutbox() {
        UUID submissionId = UUID.randomUUID();
        CommunicationEvent event = new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.CERTIFICATE_APPROVED,
                Instant.now(),
                null,
                employee.getId(),
                new CommunicationEntityRef("CERTIFICATE_SUBMISSION", submissionId, "/submissions"),
                Map.of(
                        "title", "Certificate approved",
                        "message", "Your certificate submission was approved."
                ),
                Set.of(CommunicationChannel.IN_APP, CommunicationChannel.EMAIL),
                CommunicationPriority.NORMAL,
                "integration-cert-approved-" + UUID.randomUUID()
        );

        communicationService.publish(event);

        assertThat(notificationRepository.findAll().stream()
                .anyMatch(notification -> notification.getUser().getId().equals(employee.getId())
                        && notification.getMessage().contains("approved"))).isTrue();

        assertThat(outboxRepository.count()).isEqualTo(1);
        assertThat(outboxRepository.findAll().getFirst().getStatus()).isEqualTo(CommunicationOutboxStatus.PENDING);

        outboxProcessor.processOutbox();

        assertThat(outboxRepository.findAll().getFirst().getStatus()).isEqualTo(CommunicationOutboxStatus.SENT);
    }

    @Test
    void publishSkipsDuplicateEmailOutboxEntry() {
        String idempotencyKey = "integration-duplicate-" + UUID.randomUUID();
        CommunicationEvent event = baseEmailEvent(idempotencyKey);

        communicationService.publish(event);
        communicationService.publish(event);

        assertThat(outboxRepository.count()).isEqualTo(1);
    }

    private CommunicationEvent baseEmailEvent(String idempotencyKey) {
        return new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.ACCOUNT_ACTIVATED,
                Instant.now(),
                null,
                employee.getId(),
                new CommunicationEntityRef("USER", employee.getId(), "/"),
                Map.of(
                        "title", "Account activated",
                        "message", "Your account has been activated."
                ),
                Set.of(CommunicationChannel.EMAIL),
                CommunicationPriority.LOW,
                idempotencyKey
        );
    }
}
