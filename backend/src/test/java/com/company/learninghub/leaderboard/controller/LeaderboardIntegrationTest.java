package com.company.learninghub.leaderboard.controller;

import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.domain.CertificateDocument;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.submission.repository.CertificateDocumentRepository;
import com.company.learninghub.submission.repository.CertificateSubmissionRepository;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class LeaderboardIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LearningInitiativeRepository initiativeRepository;

    @Autowired
    private CertificateSubmissionRepository submissionRepository;

    @Autowired
    private CertificateDocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String employeeToken;
    private User admin;
    private User employee;
    private LearningInitiative activeInitiative;
    private LearningInitiative draftInitiative;
    private LearningInitiative secondActiveInitiative;

    @BeforeEach
    void setUp() throws Exception {
        submissionRepository.deleteAll();
        documentRepository.deleteAll();
        initiativeRepository.deleteAll();

        admin = userRepository.findByEmailIgnoreCase("admin@learninghub.local").orElseThrow();
        employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();

        activeInitiative = initiativeRepository.save(new LearningInitiative(
                "Leaderboard Active Initiative",
                "Visible initiative for leaderboard tests",
                "Badge",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                InitiativeStatus.ACTIVE,
                admin
        ));
        secondActiveInitiative = initiativeRepository.save(new LearningInitiative(
                "Leaderboard Second Initiative",
                "Second visible initiative",
                "Badge",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                InitiativeStatus.ACTIVE,
                admin
        ));
        draftInitiative = initiativeRepository.save(new LearningInitiative(
                "Leaderboard Draft Initiative",
                "Draft initiative",
                "Badge",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                InitiativeStatus.DRAFT,
                admin
        ));

        adminToken = login("admin@learninghub.local", "Admin@12345");
        employeeToken = login("employee@learninghub.local", "Employee@12345");
    }

    @Test
    void globalLeaderboardIncludesOnlyApprovedCertificationsAndOrdersByCount() throws Exception {
        saveApprovedSubmission(employee, activeInitiative, admin,
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-02T10:00:00Z"));
        saveApprovedSubmission(employee, secondActiveInitiative, admin,
                Instant.parse("2026-06-03T10:00:00Z"),
                Instant.parse("2026-06-04T10:00:00Z"));
        saveApprovedSubmission(admin, activeInitiative, admin,
                Instant.parse("2026-06-05T10:00:00Z"),
                Instant.parse("2026-06-06T10:00:00Z"));
        saveSubmittedSubmission(admin, draftInitiative, Instant.parse("2026-06-07T10:00:00Z"));
        saveRejectedSubmission(admin, secondActiveInitiative, admin,
                Instant.parse("2026-06-08T10:00:00Z"),
                Instant.parse("2026-06-09T10:00:00Z"));

        mockMvc.perform(get("/api/v1/leaderboards/global")
                        .queryParam("sort", "rank,asc")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].rank").value(1))
                .andExpect(jsonPath("$.content[0].employee.email").value("employee@learninghub.local"))
                .andExpect(jsonPath("$.content[0].totalApprovedCertifications").value(2))
                .andExpect(jsonPath("$.content[1].rank").value(2))
                .andExpect(jsonPath("$.content[1].employee.email").value("admin@learninghub.local"))
                .andExpect(jsonPath("$.content[1].totalApprovedCertifications").value(1));
    }

    @Test
    void initiativeLeaderboardOrdersBySubmittedAtAscending() throws Exception {
        saveApprovedSubmission(admin, activeInitiative, admin,
                Instant.parse("2026-06-05T10:00:00Z"),
                Instant.parse("2026-06-06T10:00:00Z"));
        saveApprovedSubmission(employee, activeInitiative, admin,
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-02T10:00:00Z"));

        mockMvc.perform(get("/api/v1/leaderboards/initiatives/" + activeInitiative.getId())
                        .queryParam("sort", "rank,asc")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].rank").value(1))
                .andExpect(jsonPath("$.content[0].employee.email").value("employee@learninghub.local"))
                .andExpect(jsonPath("$.content[1].rank").value(2))
                .andExpect(jsonPath("$.content[1].employee.email").value("admin@learninghub.local"));
    }

    @Test
    void employeeReceivesNotFoundForDraftInitiativeLeaderboard() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboards/initiatives/" + draftInitiative.getId())
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanViewDraftInitiativeLeaderboard() throws Exception {
        saveApprovedSubmission(admin, draftInitiative, admin,
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-02T10:00:00Z"));

        mockMvc.perform(get("/api/v1/leaderboards/initiatives/" + draftInitiative.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[*].employee.email", hasItem("admin@learninghub.local")));
    }

    @Test
    void personalRankingReturnsNullRankWhenNoApprovedCertifications() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboards/me")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.globalRank").isEmpty())
                .andExpect(jsonPath("$.totalApprovedCertifications").value(0))
                .andExpect(jsonPath("$.recentApprovals", hasSize(0)));
    }

    @Test
    void personalRankingReflectsApprovedCertificationCount() throws Exception {
        saveApprovedSubmission(employee, activeInitiative, admin,
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-02T10:00:00Z"));

        mockMvc.perform(get("/api/v1/leaderboards/me")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.globalRank").value(1))
                .andExpect(jsonPath("$.totalApprovedCertifications").value(1))
                .andExpect(jsonPath("$.recentApprovals", hasSize(1)));
    }

    private CertificateSubmission saveApprovedSubmission(
            User submitter,
            LearningInitiative initiative,
            User reviewer,
            Instant submittedAt,
            Instant approvedAt
    ) {
        CertificateSubmission submission = new CertificateSubmission(
                submitter,
                initiative,
                saveDocument(submitter),
                null,
                submittedAt
        );
        submission.approve(reviewer, approvedAt);
        return submissionRepository.save(submission);
    }

    private CertificateSubmission saveSubmittedSubmission(
            User submitter,
            LearningInitiative initiative,
            Instant submittedAt
    ) {
        CertificateSubmission submission = new CertificateSubmission(
                submitter,
                initiative,
                saveDocument(submitter),
                null,
                submittedAt
        );
        return submissionRepository.save(submission);
    }

    private CertificateSubmission saveRejectedSubmission(
            User submitter,
            LearningInitiative initiative,
            User reviewer,
            Instant submittedAt,
            Instant reviewedAt
    ) {
        CertificateSubmission submission = new CertificateSubmission(
                submitter,
                initiative,
                saveDocument(submitter),
                null,
                submittedAt
        );
        submission.reject(reviewer, reviewedAt, "Invalid certificate");
        return submissionRepository.save(submission);
    }

    private CertificateDocument saveDocument(User uploader) {
        return documentRepository.save(new CertificateDocument(
                "local",
                "certificates/" + UUID.randomUUID(),
                "certificate.pdf",
                "application/pdf",
                1024,
                uploader
        ));
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
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }
}
