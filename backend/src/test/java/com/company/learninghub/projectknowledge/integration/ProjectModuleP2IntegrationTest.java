package com.company.learninghub.projectknowledge.integration;

import com.company.learninghub.auth.dto.LoginRequest;
import com.company.learninghub.auth.dto.LoginResponse;
import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.dto.CreateProjectLinkRequest;
import com.company.learninghub.projectknowledge.dto.UpdateProjectItemRequest;
import com.company.learninghub.projectknowledge.dto.CreateProjectRequest;
import com.company.learninghub.projectknowledge.dto.ProjectFolderRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class ProjectModuleP2IntegrationTest {

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
    void knowledgeBaseFoldersLinksSearchAndPermissions() throws Exception {
        String adminToken = loginToken("admin@learninghub.local", "Admin@12345");
        String employeeToken = loginToken("employee@learninghub.local", "Employee@12345");

        MvcResult createProject = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProjectRequest("P2 Knowledge Project", "KB test", ProjectAccessType.MEMBERS_ONLY)
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        UUID projectId = UUID.fromString(
                objectMapper.readTree(createProject.getResponse().getContentAsString()).get("id").asText()
        );

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/folders")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());

        MvcResult requirementsFolder = mockMvc.perform(post("/api/v1/projects/" + projectId + "/folders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectFolderRequest("Requirements", "Req docs", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.childFolderCount").value(0))
                .andReturn();

        UUID requirementsId = UUID.fromString(
                objectMapper.readTree(requirementsFolder.getResponse().getContentAsString()).get("id").asText()
        );

        MvcResult architectureFolder = mockMvc.perform(post("/api/v1/projects/" + projectId + "/folders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectFolderRequest("Architecture", "Arch docs", requirementsId)
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        UUID architectureId = UUID.fromString(
                objectMapper.readTree(architectureFolder.getResponse().getContentAsString()).get("id").asText()
        );

        mockMvc.perform(post("/api/v1/projects/" + projectId + "/folders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectFolderRequest("Blocked", "Too deep", architectureId)
                        )))
                .andExpect(status().isBadRequest());

        MvcResult linkResult = mockMvc.perform(post("/api/v1/projects/" + projectId + "/items/links")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectLinkRequest(
                                architectureId,
                                "API Documentation",
                                "Swagger",
                                KnowledgeCategory.KT_DOCUMENTS,
                                "https://example.com/api-docs"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        UUID itemId = UUID.fromString(
                objectMapper.readTree(linkResult.getResponse().getContentAsString()).get("id").asText()
        );

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/items")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "api")
                        .param("sourceType", "LINK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("API Documentation"));

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/folders/" + architectureId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1));

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/items/" + itemId + "/link")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCount").value(1));

        mockMvc.perform(put("/api/v1/projects/" + projectId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProjectItemRequest(
                                architectureId,
                                "Updated API Docs",
                                "Swagger",
                                KnowledgeCategory.KT_DOCUMENTS,
                                "https://example.com/api-docs"
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/projects/" + projectId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/projects/" + projectId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/projects/" + projectId + "/folders/" + architectureId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        JsonNode folders = objectMapper.readTree(mockMvc.perform(get("/api/v1/projects/" + projectId + "/folders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(folders.get("content")).isNotEmpty();
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
