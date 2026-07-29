package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiCompatibleClientTest {

    private HttpServer server;
    private String lastAuthorization;
    private String lastRequestBody;
    private int responseStatus = 200;
    private String responseBody = """
            {
              "id": "chatcmpl_test",
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "Docker is a container platform."
                  }
                }
              ]
            }
            """;
    private OpenAiCompatibleClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", this::handleChatCompletions);
        server.start();

        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().setProvider("openai-compatible");
        properties.getLlm().getOpenaiCompatible().setApiKey("test-api-key");
        properties.getLlm().getOpenaiCompatible().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.getLlm().getOpenaiCompatible().setModel("gpt-4o-mini");
        properties.getLlm().getOpenaiCompatible().setConnectTimeout(Duration.ofSeconds(2));
        properties.getLlm().getOpenaiCompatible().setReadTimeout(Duration.ofSeconds(2));

        client = new OpenAiCompatibleClient(properties, new ObjectMapper(), HttpClient.newHttpClient());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void completeReturnsSuccessForSuccessfulApiResponse() {
        LlmCompletionRequest request = new LlmCompletionRequest(
                "You are a helpful assistant.",
                List.of(new LlmCompletionRequest.LlmMessage("user", "what is docker"))
        );

        LlmCompletionResult result = client.complete(request);

        assertThat(result.success()).isTrue();
        assertThat(result.content()).isEqualTo("Docker is a container platform.");
        assertThat(result.providerReference()).isEqualTo("chatcmpl_test");
        assertThat(lastAuthorization).isEqualTo("Bearer test-api-key");
        assertThat(lastRequestBody).contains("\"model\":\"gpt-4o-mini\"");
        assertThat(lastRequestBody).contains("\"role\":\"system\"");
        assertThat(lastRequestBody).contains("what is docker");
        assertThat(client.providerName()).isEqualTo("openai-compatible");
        assertThat(client.isHealthy()).isTrue();
    }

    @Test
    void completeReturnsFailureForApiErrorResponse() {
        responseStatus = 401;
        responseBody = "{\"error\":{\"message\":\"Incorrect API key provided\"}}";

        LlmCompletionResult result = client.complete(sampleRequest());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("401");
        assertThat(result.errorMessage()).contains("Incorrect API key provided");
    }

    @Test
    void completeSucceedsWithoutApiKeyForLocalCompatibleProviders() {
        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().getOpenaiCompatible().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.getLlm().getOpenaiCompatible().setModel("qwen3:8b");
        OpenAiCompatibleClient localClient = new OpenAiCompatibleClient(
                properties,
                new ObjectMapper(),
                HttpClient.newHttpClient()
        );

        LlmCompletionResult result = localClient.complete(sampleRequest());

        assertThat(result.success()).isTrue();
        assertThat(result.content()).isEqualTo("Docker is a container platform.");
        assertThat(lastAuthorization).isNull();
        assertThat(lastRequestBody).contains("\"reasoning_effort\":\"none\"");
        assertThat(localClient.isHealthy()).isTrue();
    }

    @Test
    void completeUsesReasoningFieldWhenContentEmptyForOllamaThinkingModels() {
        responseBody = """
                {
                  "id": "chatcmpl_qwen3",
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "",
                        "reasoning": "Spring Boot is a Java framework for building production-ready applications."
                      }
                    }
                  ]
                }
                """;

        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().getOpenaiCompatible().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.getLlm().getOpenaiCompatible().setModel("qwen3:8b");
        OpenAiCompatibleClient ollamaClient = new OpenAiCompatibleClient(
                properties,
                new ObjectMapper(),
                HttpClient.newHttpClient()
        );

        LlmCompletionResult result = ollamaClient.complete(sampleRequest());

        assertThat(result.success()).isTrue();
        assertThat(result.content()).isEqualTo(
                "Spring Boot is a Java framework for building production-ready applications."
        );
        assertThat(lastRequestBody).contains("\"reasoning_effort\":\"none\"");
    }

    @Test
    void isOllamaCompatibleEndpointDetectsCommonLocalHosts() {
        assertThat(OpenAiCompatibleClient.isOllamaCompatibleEndpoint("http://localhost:11434")).isTrue();
        assertThat(OpenAiCompatibleClient.isOllamaCompatibleEndpoint("http://host.docker.internal:11434")).isTrue();
        assertThat(OpenAiCompatibleClient.isOllamaCompatibleEndpoint("https://api.openai.com")).isFalse();
    }

    @Test
    void isHealthyReturnsFalseWhenModelMissing() {
        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().getOpenaiCompatible().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.getLlm().getOpenaiCompatible().setModel("");
        OpenAiCompatibleClient unhealthyClient = new OpenAiCompatibleClient(
                properties,
                new ObjectMapper(),
                HttpClient.newHttpClient()
        );

        assertThat(unhealthyClient.isHealthy()).isFalse();
    }

    @Test
    void completeReturnsFailureWhenBaseUrlMissing() {
        AssistantProperties properties = new AssistantProperties();
        properties.getLlm().getOpenaiCompatible().setBaseUrl("");
        properties.getLlm().getOpenaiCompatible().setApiKey("test-api-key");
        OpenAiCompatibleClient missingBaseUrlClient = new OpenAiCompatibleClient(
                properties,
                new ObjectMapper(),
                HttpClient.newHttpClient()
        );

        LlmCompletionResult result = missingBaseUrlClient.complete(sampleRequest());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("base URL is not configured");
        assertThat(missingBaseUrlClient.isHealthy()).isFalse();
    }

    @Test
    void completeReturnsFailureForTimeout() {
        server.removeContext("/v1/chat/completions");
        server.createContext("/v1/chat/completions", exchange -> {
            try {
                Thread.sleep(3_000L);
                writeResponse(exchange, 200, responseBody);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        LlmCompletionResult result = client.complete(sampleRequest());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("timed out");
    }

    private LlmCompletionRequest sampleRequest() {
        return new LlmCompletionRequest(
                "System prompt",
                List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        );
    }

    private void handleChatCompletions(HttpExchange exchange) throws IOException {
        lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
        lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        writeResponse(exchange, responseStatus, responseBody);
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
