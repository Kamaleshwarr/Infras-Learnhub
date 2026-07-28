package com.company.learninghub.auth.integration;

import com.company.learninghub.auth.domain.PasswordResetToken;
import com.company.learninghub.auth.repository.PasswordResetTokenRepository;
import com.company.learninghub.auth.service.PasswordResetService;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import com.company.learninghub.communication.repository.CommunicationOutboxRepository;
import com.company.learninghub.communication.service.CommunicationEventSerializer;
import com.company.learninghub.communication.service.CommunicationOutboxProcessor;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetCommunicationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.communication.enabled", () -> "true");
        registry.add("app.communication.email.provider", () -> "log");
        registry.add("app.catalog.import.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private CommunicationOutboxRepository outboxRepository;

    @Autowired
    private CommunicationOutboxProcessor outboxProcessor;

    @Autowired
    private CommunicationEventSerializer eventSerializer;

    @BeforeEach
    void resetEmployeePasswordState() {
        outboxRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        employee.setPasswordHash(passwordEncoder.encode("Employee@12345"));
        employee.setMustChangePassword(false);
        employee.setPasswordChangedAt(Instant.parse("2026-01-01T00:00:00Z"));
        employee.setActive(true);
        userRepository.save(employee);
    }

    @Test
    void forgotPasswordPublishesCommunicationEventAndProcessesEmailOutbox() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "employee@learninghub.local"}
                                """))
                .andExpect(status().isAccepted());

        assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findAll().getFirst().getEventType())
                .isEqualTo(CommunicationEventType.PASSWORD_RESET_REQUESTED);
        assertThat(outboxRepository.findAll().getFirst().getStatus())
                .isEqualTo(CommunicationOutboxStatus.PENDING);
        assertThat(outboxRepository.findAll().getFirst().getPayloadJson()).contains("reset-password?token=");

        outboxProcessor.processOutbox();

        assertThat(outboxRepository.findAll().getFirst().getStatus())
                .isEqualTo(CommunicationOutboxStatus.SENT);
    }

    @Test
    void resetPasswordCompletesAfterForgotPasswordFlow() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "employee@learninghub.local"}
                                """))
                .andExpect(status().isAccepted());

        PasswordResetToken storedToken = passwordResetTokenRepository.findAll().getFirst();
        String rawToken = extractRawTokenFromOutbox();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "ResetSecure1!",
                                  "confirmNewPassword": "ResetSecure1!"
                                }
                                """.formatted(rawToken)))
                .andExpect(status().isNoContent());

        PasswordResetToken updatedToken = passwordResetTokenRepository.findById(storedToken.getId()).orElseThrow();
        assertThat(updatedToken.isUsed()).isTrue();
    }

    private String extractRawTokenFromOutbox() {
        String payload = outboxRepository.findAll().getFirst().getPayloadJson();
        CommunicationEvent event = eventSerializer.deserialize(payload);
        String resetUrl = event.variables().get("resetUrl");
        int tokenIndex = resetUrl.indexOf("token=");
        if (tokenIndex < 0) {
            throw new IllegalStateException("Reset URL missing token parameter: " + resetUrl);
        }
        return resetUrl.substring(tokenIndex + "token=".length());
    }
}
