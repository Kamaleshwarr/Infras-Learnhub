package com.company.learninghub.assistant.integration;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.config.LlmClientConfiguration;
import com.company.learninghub.assistant.diagnostics.AssistantChatDiagnostics;
import com.company.learninghub.assistant.dto.AssistantLlmDebugResponse;
import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.IntentResolver;
import com.company.learninghub.assistant.intent.NavigationIntentResolver;
import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.llm.MockLlmClient;
import com.company.learninghub.assistant.llm.OpenAiCompatibleClient;
import com.company.learninghub.assistant.llm.PromptOrchestrator;
import com.company.learninghub.assistant.service.AssistantConversationService;
import com.company.learninghub.assistant.service.AssistantOrchestrationService;
import com.company.learninghub.assistant.tool.AssistantToolRegistry;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Definitive runtime diagnosis against live Ollama when available.
 */
class AssistantRuntimeDiagnosisTest {

    private static final Logger log = LoggerFactory.getLogger(AssistantRuntimeDiagnosisTest.class);
    private static final String OLLAMA_BASE_URL = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://127.0.0.1:11434");
    private static final String OLLAMA_MODEL = System.getenv().getOrDefault("OLLAMA_MODEL", "qwen3:4b");

    @BeforeAll
    static void requireOllama() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(OLLAMA_BASE_URL + "/api/tags")).GET().timeout(Duration.ofSeconds(3)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assumptions.assumeTrue(response.statusCode() == 200, "Ollama not reachable at " + OLLAMA_BASE_URL);
        Assumptions.assumeTrue(response.body().contains(OLLAMA_MODEL), "Model not installed: " + OLLAMA_MODEL);
    }

    @Test
    void debugProbeCallsOpenAiCompatibleClientDirectly() {
        OpenAiCompatibleClient client = createOpenAiClient();

        AssistantLlmDebugResponse debug = client.probeDirectLlmCall();

        log.info("RUNTIME_DIAG /assistant/debug equivalent: provider={} success={} httpStatus={} elapsedMs={} httpUri={} error={}",
                debug.provider(), debug.success(), debug.httpStatus(), debug.elapsedMs(), debug.httpUri(), debug.error());
        log.info("RUNTIME_DIAG parsedContent={}", debug.parsedContent());
        log.info("RUNTIME_DIAG parsedReasoning={}", debug.parsedReasoning());
        log.info("RUNTIME_DIAG finalText={}", debug.finalText());
        log.info("RUNTIME_DIAG rawResponse={}", debug.rawResponse());

        assertThat(debug.provider()).isEqualTo("openai-compatible");
        assertThat(debug.httpStatus()).isEqualTo(200);
        assertThat(debug.success()).isTrue();
        assertThat(debug.finalText()).isNotBlank();
    }

    @Test
    void chatKnowledgeQuestionsProduceDiagnosticTrace() {
        AssistantOrchestrationService service = createOrchestrationService();
        AuthenticatedUser user = authenticatedUser();

        for (String question : List.of("What is Algebra?", "What is Spring Boot?")) {
            AssistantChatDiagnostics.start("runtime-diagnosis-" + question.hashCode());
            try {
                var response = service.processRequest(new AssistantRequest(question, null), user);
                log.info("RUNTIME_DIAG question={} intent={} response={}",
                        question,
                        response.intentType(),
                        AssistantChatDiagnostics.truncate(response.message(), 500));
                assertThat(response.intentType()).isEqualTo(AssistantIntentType.KNOWLEDGE);
            } finally {
                AssistantChatDiagnostics.end();
            }
        }
    }

    private static OpenAiCompatibleClient createOpenAiClient() {
        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().setProvider("openai-compatible");
        properties.getLlm().getOpenaiCompatible().setBaseUrl(OLLAMA_BASE_URL);
        properties.getLlm().getOpenaiCompatible().setModel(OLLAMA_MODEL);
        properties.getLlm().getOpenaiCompatible().setApiKey("");
        return new OpenAiCompatibleClient(properties, new ObjectMapper());
    }

    private static AssistantOrchestrationService createOrchestrationService() {
        AssistantProperties properties = new AssistantProperties();
        properties.setEnabled(true);
        properties.getLlm().setProvider("openai-compatible");
        properties.getLlm().getOpenaiCompatible().setBaseUrl(OLLAMA_BASE_URL);
        properties.getLlm().getOpenaiCompatible().setModel(OLLAMA_MODEL);
        properties.getLlm().getOpenaiCompatible().setApiKey("");

        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiCompatibleClient openAiCompatibleClient = new OpenAiCompatibleClient(properties, objectMapper);
        LlmClient llmClient = new LlmClientConfiguration().llmClient(
                properties,
                new MockLlmClient(),
                openAiCompatibleClient
        );

        return new AssistantOrchestrationService(
                properties,
                llmClient,
                new PromptOrchestrator(objectMapper),
                new IntentResolver(new NavigationIntentResolver()),
                mock(AssistantToolRegistry.class),
                mock(AssistantConversationService.class)
        );
    }

    private static AuthenticatedUser authenticatedUser() {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return AuthenticatedUser.from(user);
    }
}
