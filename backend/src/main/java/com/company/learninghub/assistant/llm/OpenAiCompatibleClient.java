package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleClient implements LlmClient {

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private final AssistantProperties assistantProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public OpenAiCompatibleClient(AssistantProperties assistantProperties, ObjectMapper objectMapper) {
        this(assistantProperties, objectMapper, buildHttpClient(assistantProperties));
    }

    OpenAiCompatibleClient(
            AssistantProperties assistantProperties,
            ObjectMapper objectMapper,
            HttpClient httpClient
    ) {
        this.assistantProperties = assistantProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public LlmCompletionResult complete(LlmCompletionRequest request) {
        AssistantProperties.OpenAiCompatible config = assistantProperties.getLlm().getOpenaiCompatible();
        if (!StringUtils.hasText(config.getApiKey())) {
            return LlmCompletionResult.failure("OpenAI-compatible API key is not configured");
        }
        if (!StringUtils.hasText(config.getBaseUrl())) {
            return LlmCompletionResult.failure("OpenAI-compatible base URL is not configured");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestBody(request, config));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(resolveChatCompletionsUri(config.getBaseUrl()))
                    .timeout(config.getReadTimeout())
                    .header("Authorization", "Bearer " + config.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseSuccessResponse(response.body());
            }
            return LlmCompletionResult.failure(extractErrorMessage(response.statusCode(), response.body()));
        } catch (HttpTimeoutException ex) {
            return LlmCompletionResult.failure("OpenAI-compatible request timed out");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return LlmCompletionResult.failure("OpenAI-compatible request interrupted");
        } catch (IOException ex) {
            return LlmCompletionResult.failure("OpenAI-compatible request failed: " + ex.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        AssistantProperties.OpenAiCompatible config = assistantProperties.getLlm().getOpenaiCompatible();
        return StringUtils.hasText(config.getApiKey()) && StringUtils.hasText(config.getBaseUrl());
    }

    @Override
    public String providerName() {
        return "openai-compatible";
    }

    private Map<String, Object> buildRequestBody(
            LlmCompletionRequest request,
            AssistantProperties.OpenAiCompatible config
    ) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (StringUtils.hasText(request.systemPrompt())) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }
        if (request.messages() != null) {
            for (LlmCompletionRequest.LlmMessage message : request.messages()) {
                if (message == null || !StringUtils.hasText(message.role()) || message.content() == null) {
                    continue;
                }
                messages.add(Map.of("role", message.role().toLowerCase(), "content", message.content()));
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("temperature", 0.2);
        return body;
    }

    private LlmCompletionResult parseSuccessResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return LlmCompletionResult.failure("OpenAI-compatible response did not include any choices");
        }

        JsonNode messageNode = choices.get(0).path("message");
        String content = messageNode.path("content").asText(null);
        if (!StringUtils.hasText(content)) {
            return LlmCompletionResult.failure("OpenAI-compatible response did not include message content");
        }

        String providerReference = root.path("id").asText("openai-compatible");
        return LlmCompletionResult.success(content.trim(), providerReference);
    }

    private String extractErrorMessage(int statusCode, String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "OpenAI-compatible API returned HTTP " + statusCode;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorNode = root.path("error");
            String message = errorNode.isObject()
                    ? textValue(errorNode, "message")
                    : textValue(root, "message");
            if (StringUtils.hasText(message)) {
                return "OpenAI-compatible API error (" + statusCode + "): " + message;
            }
        } catch (IOException ignored) {
            // Fall back to raw response body below.
        }
        return "OpenAI-compatible API error (" + statusCode + "): " + responseBody;
    }

    private String textValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node == null || node.isNull() ? null : node.asText();
    }

    private URI resolveChatCompletionsUri(String baseUrl) {
        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return URI.create(normalizedBase + CHAT_COMPLETIONS_PATH);
    }

    private static HttpClient buildHttpClient(AssistantProperties assistantProperties) {
        Duration connectTimeout = assistantProperties.getLlm().getOpenaiCompatible().getConnectTimeout();
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }
}
