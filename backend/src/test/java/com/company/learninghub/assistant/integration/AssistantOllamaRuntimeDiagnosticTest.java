package com.company.learninghub.assistant.integration;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.config.LlmClientConfiguration;
import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.llm.LlmCompletionRequest;
import com.company.learninghub.assistant.llm.LlmCompletionResult;
import com.company.learninghub.assistant.llm.MockLlmClient;
import com.company.learninghub.assistant.llm.OpenAiCompatibleClient;
import com.company.learninghub.assistant.llm.PromptOrchestrator;
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

/**
 * Runtime diagnostic against a live Ollama instance when available on localhost:11434.
 */
class AssistantOllamaRuntimeDiagnosticTest {

    private static final Logger log = LoggerFactory.getLogger(AssistantOllamaRuntimeDiagnosticTest.class);
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
    void diagnosticTraceForKnowledgeQuestions() {
        AssistantProperties properties = createProperties(OLLAMA_BASE_URL);
        ObjectMapper objectMapper = new ObjectMapper();
        MockLlmClient mockLlmClient = new MockLlmClient();
        OpenAiCompatibleClient openAiCompatibleClient = new OpenAiCompatibleClient(properties, objectMapper);
        LlmClient llmClient = new LlmClientConfiguration().llmClient(properties, mockLlmClient, openAiCompatibleClient);

        log.info("DIAG injected llmClient class={}", llmClient.getClass().getName());
        log.info("DIAG providerName={}", llmClient.providerName());
        assertThat(llmClient).isInstanceOf(OpenAiCompatibleClient.class);
        assertThat(llmClient.providerName()).isEqualTo("openai-compatible");

        PromptOrchestrator promptOrchestrator = new PromptOrchestrator(objectMapper);
        for (String question : List.of("What is Algebra?", "What is Spring Boot?")) {
            LlmCompletionRequest request = promptOrchestrator.buildKnowledgeRequest(question, List.of());
            log.info("DIAG question={} systemPromptLength={} messageCount={}",
                    question,
                    request.systemPrompt().length(),
                    request.messages().size());

            LlmCompletionResult result = llmClient.complete(request);
            log.info(
                    "DIAG llmClient.complete question={} success={} error={} content={}",
                    question,
                    result.success(),
                    result.errorMessage(),
                    truncate(result.content(), 300)
            );

            assertThat(result.success())
                    .withFailMessage("LLM failed for '%s': %s", question, result.errorMessage())
                    .isTrue();
            assertThat(result.content())
                    .withFailMessage("Empty content for '%s'", question)
                    .isNotBlank();
            assertThat(result.content()).doesNotContain("don't currently have enough information");
        }
    }

    @Test
    void diagnosticDoubleV1BaseUrlSucceedsAfterNormalization() {
        AssistantProperties properties = createProperties(OLLAMA_BASE_URL + "/v1");
        OpenAiCompatibleClient client = new OpenAiCompatibleClient(properties, new ObjectMapper());

        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "system",
                List.of(new LlmCompletionRequest.LlmMessage("user", "What is Spring Boot?"))
        ));

        log.info("DIAG doubleV1 success={} error={} content={}", result.success(), result.errorMessage(), truncate(result.content(), 200));
        assertThat(result.success()).isTrue();
        assertThat(result.content()).isNotBlank();
    }

    private static AssistantProperties createProperties(String baseUrl) {
        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().setProvider("openai-compatible");
        properties.getLlm().getOpenaiCompatible().setBaseUrl(baseUrl);
        properties.getLlm().getOpenaiCompatible().setModel(OLLAMA_MODEL);
        properties.getLlm().getOpenaiCompatible().setApiKey("");
        properties.getLlm().getOpenaiCompatible().setConnectTimeout(Duration.ofSeconds(10));
        properties.getLlm().getOpenaiCompatible().setReadTimeout(Duration.ofSeconds(120));
        return properties;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
