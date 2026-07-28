package com.company.learninghub.assistant.integration;

import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantResponse;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.dto.ConversationResponse;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.auth.dto.LoginRequest;
import com.company.learninghub.auth.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class AssistantIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_assistant")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.catalog.import.enabled", () -> "false");
        registry.add("app.assistant.enabled", () -> "true");
        registry.add("app.assistant.llm.provider", () -> "mock");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String accessToken;

    @BeforeEach
    void setUp() {
        LoginRequest loginRequest = new LoginRequest("employee@learninghub.local", "Employee@12345");
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                loginRequest,
                LoginResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        accessToken = loginResponse.getBody().accessToken();
    }

    @Test
    void statusReturnsAssistantAvailability() {
        ResponseEntity<AssistantStatusResponse> response = restTemplate.exchange(
                baseUrl() + "/assistant/status",
                HttpMethod.GET,
                authenticatedEntity(null),
                AssistantStatusResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().enabled()).isTrue();
        assertThat(response.getBody().llmProvider()).isEqualTo("mock");
        assertThat(response.getBody().llmHealthy()).isTrue();
    }

    @Test
    void chatAndConversationFlowUsesMockProvider() {
        AssistantRequest chatRequest = new AssistantRequest("what is docker", null);
        ResponseEntity<AssistantResponse> chatResponse = restTemplate.exchange(
                baseUrl() + "/assistant/chat",
                HttpMethod.POST,
                authenticatedEntity(chatRequest),
                AssistantResponse.class
        );

        assertThat(chatResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(chatResponse.getBody().response()).containsIgnoringCase("docker");
        assertThat(chatResponse.getBody().intentType()).isEqualTo(AssistantIntentType.KNOWLEDGE);
        assertThat(chatResponse.getBody().conversationId()).isNotNull();

        ResponseEntity<ConversationResponse> conversationResponse = restTemplate.exchange(
                baseUrl() + "/assistant/conversation",
                HttpMethod.GET,
                authenticatedEntity(null),
                ConversationResponse.class
        );

        assertThat(conversationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(conversationResponse.getBody().messages()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(conversationResponse.getBody().messages().getFirst().content()).isEqualTo("what is docker");
    }

    @Test
    void toolChatReturnsGroundedProfileResponse() {
        AssistantRequest chatRequest = new AssistantRequest("my profile", null);
        ResponseEntity<AssistantResponse> chatResponse = restTemplate.exchange(
                baseUrl() + "/assistant/chat",
                HttpMethod.POST,
                authenticatedEntity(chatRequest),
                AssistantResponse.class
        );

        assertThat(chatResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(chatResponse.getBody().intentType()).isEqualTo(AssistantIntentType.TOOL);
        assertThat(chatResponse.getBody().toolUsed()).isEqualTo("my-profile");
        assertThat(chatResponse.getBody().response()).contains("employee@learninghub.local");
        assertThat(chatResponse.getBody().sources()).hasSize(1);
        assertThat(chatResponse.getBody().sources().getFirst().confidence().name()).isEqualTo("HIGH");
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private <T> HttpEntity<T> authenticatedEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
