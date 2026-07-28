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
              "id": "chatcmpl-test",
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
    private AssistantProperties assistantProperties;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", this::handleChatCompletions);
        server.start();

        assistantProperties = new AssistantProperties();
        assistantProperties.getLlm().setProvider("openai-compatible");
        AssistantProperties.OpenAiCompatible config = assistantProperties.getLlm().getOpenaiCompatible();
        config.setApiKey("test-api-key");
        config.setBaseUrl("http://localhost:" + server.getAddress().getPort());
        config.setModel("gpt-4o-mini");
        config.setConnectTimeout(Duration.ofSeconds(2));
        config.setReadTimeout(Duration.ofSeconds(2));

        client = new OpenAiCompatibleClient(assistantProperties, new ObjectMapper(), HttpClient.newHttpClient());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void completeReturnsSuccessForOpenAiStyleResponse() {
        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "You are helpful.",
                List.of(new LlmCompletionRequest.LlmMessage("user", "What is Docker?"))
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.content()).isEqualTo("Docker is a container platform.");
        assertThat(result.providerReference()).isEqualTo("chatcmpl-test");
        assertThat(lastAuthorization).isEqualTo("Bearer test-api-key");
        assertThat(lastRequestBody).contains("\"model\":\"gpt-4o-mini\"");
        assertThat(lastRequestBody).contains("\"stream\":false");
        assertThat(lastRequestBody).contains("What is Docker?");
        assertThat(client.providerName()).isEqualTo("openai-compatible");
        assertThat(client.isHealthy()).isTrue();
    }

    @Test
    void completeSupportsBaseUrlEndingWithV1() {
        assistantProperties.getLlm().getOpenaiCompatible()
                .setBaseUrl("http://localhost:" + server.getAddress().getPort() + "/v1");

        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "system",
                List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.content()).isEqualTo("Docker is a container platform.");
    }

    @Test
    void completeOmitsAuthorizationWhenApiKeyMissing() {
        assistantProperties.getLlm().getOpenaiCompatible().setApiKey("");

        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "system",
                List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        ));

        assertThat(result.success()).isTrue();
        assertThat(lastAuthorization).isNull();
        assertThat(client.isHealthy()).isTrue();
    }

    @Test
    void completeReturnsFailureForApiErrorResponse() {
        responseStatus = 401;
        responseBody = "{\"error\":{\"message\":\"Invalid API key\"}}";

        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "system",
                List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("401");
        assertThat(result.errorMessage()).contains("Invalid API key");
    }

    @Test
    void completeReturnsFailureWhenBaseUrlMissing() {
        assistantProperties.getLlm().getOpenaiCompatible().setBaseUrl("");

        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "system",
                List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("base URL is not configured");
        assertThat(client.isHealthy()).isFalse();
    }

    @Test
    void completeReturnsFailureWhenModelMissing() {
        assistantProperties.getLlm().getOpenaiCompatible().setModel("");

        LlmCompletionResult result = client.complete(new LlmCompletionRequest(
                "system",
                List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("model is not configured");
        assertThat(client.isHealthy()).isFalse();
    }

    private void handleChatCompletions(HttpExchange exchange) throws IOException {
        lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
        lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }
}
