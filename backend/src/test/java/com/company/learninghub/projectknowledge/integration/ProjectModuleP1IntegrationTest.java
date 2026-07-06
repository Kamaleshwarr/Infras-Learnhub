package com.company.learninghub.projectknowledge.integration;

import com.company.learninghub.auth.dto.LoginRequest;
import com.company.learninghub.auth.dto.LoginResponse;
import com.company.learninghub.learn.dto.TechnologyProjectLinkRequest;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.dto.CreateProjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class ProjectModuleP1IntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.catalog.import.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanCreateProjectEmployeeCannotAndTechnologyCrossNavigationRespectsAccess() throws Exception {
        String adminToken = loginToken("admin@learninghub.local", "Admin@12345");
        String employeeToken = loginToken("employee@learninghub.local", "Employee@12345");

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProjectRequest("Blocked Project", "desc", ProjectAccessType.PUBLIC)
                        )))
                .andExpect(status().isForbidden());

        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProjectRequest("P1 Integration Project", "Portal test project", ProjectAccessType.MEMBERS_ONLY)
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        JsonNode project = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID projectId = UUID.fromString(project.get("id").asText());

        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());

        MvcResult technologiesResult = mockMvc.perform(get("/api/v1/learn/technologies")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode technologies = objectMapper.readTree(technologiesResult.getResponse().getContentAsString());
        assertThat(technologies.get("content").size()).isPositive();
        UUID technologyId = UUID.fromString(technologies.get("content").get(0).get("id").asText());

        mockMvc.perform(post("/api/v1/learn/manage/technologies/" + technologyId + "/project-links")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TechnologyProjectLinkRequest(projectId))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/learn/technologies/" + technologyId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relatedProjects").isEmpty());

        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("assigned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private String loginToken(String email, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        return response.accessToken();
    }
}
