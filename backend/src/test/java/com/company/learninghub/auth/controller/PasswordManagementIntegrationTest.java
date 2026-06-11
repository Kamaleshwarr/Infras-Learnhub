package com.company.learninghub.auth.controller;

import com.company.learninghub.auth.domain.PasswordResetToken;
import com.company.learninghub.auth.repository.PasswordResetTokenRepository;
import com.company.learninghub.auth.service.PasswordResetService;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class PasswordManagementIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.mail.mode", () -> "log");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void resetEmployeePasswordState() {
        passwordResetTokenRepository.deleteAll();
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        employee.setPasswordHash(passwordEncoder.encode("Employee@12345"));
        employee.setMustChangePassword(false);
        employee.setPasswordChangedAt(Instant.parse("2026-01-01T00:00:00Z"));
        employee.setActive(true);
        userRepository.save(employee);
    }

    @Test
    void changePasswordClearsMustChangePasswordFlag() throws Exception {
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        employee.setMustChangePassword(true);
        userRepository.save(employee);

        String token = login("employee@learninghub.local", "Employee@12345");

        mockMvc.perform(get("/api/v1/initiatives")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Employee@12345",
                                  "newPassword": "NewSecure1!",
                                  "confirmNewPassword": "NewSecure1!"
                                }
                                """))
                .andExpect(status().isNoContent());

        User updated = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        assertThat(updated.isMustChangePassword()).isFalse();
        assertThat(updated.getPasswordChangedAt()).isNotNull();
    }

    @Test
    void forgotPasswordReturnsSameResponseForUnknownEmail() throws Exception {
        String knownEmailResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "employee@learninghub.local"}
                                """))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String unknownEmailResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "missing@learninghub.local"}
                                """))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(unknownEmailResponse).isEqualTo(knownEmailResponse);
    }

    @Test
    void resetPasswordRejectsReusedToken() throws Exception {
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        String rawToken = "integration-reset-token";
        passwordResetTokenRepository.save(new PasswordResetToken(
                employee,
                PasswordResetService.hashToken(rawToken),
                Instant.now().plus(Duration.ofHours(1))
        ));

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

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "AnotherSecure1!",
                                  "confirmNewPassword": "AnotherSecure1!"
                                }
                                """.formatted(rawToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void resetPasswordRejectsExpiredToken() throws Exception {
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        String rawToken = "expired-reset-token";
        passwordResetTokenRepository.save(new PasswordResetToken(
                employee,
                PasswordResetService.hashToken(rawToken),
                Instant.now().minus(Duration.ofMinutes(5))
        ));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "ResetSecure1!",
                                  "confirmNewPassword": "ResetSecure1!"
                                }
                                """.formatted(rawToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void jwtIssuedBeforePasswordChangeIsRejected() throws Exception {
        String token = login("employee@learninghub.local", "Employee@12345");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Employee@12345",
                                  "newPassword": "NewSecure1!",
                                  "confirmNewPassword": "NewSecure1!"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deactivatedUserTokenIsRejected() throws Exception {
        String token = login("employee@learninghub.local", "Employee@12345");
        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        employee.setActive(false);
        userRepository.save(employee);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

}
