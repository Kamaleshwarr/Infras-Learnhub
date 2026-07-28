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
        if (!StringUtils.hasText(config.getBaseUrl())) {
            return LlmCompletionResult.failure("OpenAI-compatible base URL is not configured");
        }
        if (!StringUtils.hasText(config.getModel())) {
            return LlmCompletionResult.failure("OpenAI-compatible model is not configured");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestBody(config, request));
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(resolveChatCompletionsUri(config.getBaseUrl()))
                    .timeout(config.getReadTimeout())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            if (StringUtils.hasText(config.getApiKey())) {
                requestBuilder.header("Authorization", "Bearer " + config.getApiKey().trim());
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseSuccessResponse(response.body());
            }
            return LlmCompletionResult.failure(extractErrorMessage(response.statusCode(), response.body()));
        } catch (HttpTimeoutException ex) {
            return LlmCompletionResult.failure("OpenAI-compatible LLM request timed out");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return LlmCompletionResult.failure("OpenAI-compatible LLM request interrupted");
        } catch (IOException ex) {
            return LlmCompletionResult.failure("OpenAI-compatible LLM request failed: " + ex.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        AssistantProperties.OpenAiCompatible config = assistantProperties.getLlm().getOpenaiCompatible();
        return StringUtils.hasText(config.getBaseUrl()) && StringUtils.hasText(config.getModel());
    }

    @Override
    public String providerName() {
        return "openai-compatible";
    }

    private Map<String, Object> buildRequestBody(AssistantProperties.OpenAiCompatible config, LlmCompletionRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (StringUtils.hasText(request.systemPrompt())) {
            messages.add(message("system", request.systemPrompt()));
        }
        if (request.messages() != null) {
            for (LlmCompletionRequest.LlmMessage message : request.messages()) {
                if (message != null && StringUtils.hasText(message.role()) && message.content() != null) {
                    messages.add(message(message.role(), message.content()));
                }
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("stream", false);
        return body;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private LlmCompletionResult parseSuccessResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = extractAssistantContent(root);
        if (!StringUtils.hasText(content)) {
            return LlmCompletionResult.failure("OpenAI-compatible LLM response did not include assistant content");
        }
        String providerReference = extractProviderReference(root);
        return LlmCompletionResult.success(content.trim(), providerReference);
    }

    private String extractAssistantContent(JsonNode root) {
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return null;
        }
        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            return null;
        }
        JsonNode content = message.get("content");
        return content == null || content.isNull() ? null : content.asText();
    }

    private String extractProviderReference(JsonNode root) {
        JsonNode idNode = root.get("id");
        if (idNode != null && StringUtils.hasText(idNode.asText())) {
            return idNode.asText();
        }
        return "openai-compatible";
    }

    private URI resolveChatCompletionsUri(String baseUrl) {
        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        if (normalizedBase.endsWith("/v1")) {
            return URI.create(normalizedBase + "/chat/completions");
        }
        return URI.create(normalizedBase + CHAT_COMPLETIONS_PATH);
    }

    private String extractErrorMessage(int statusCode, String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "OpenAI-compatible LLM API returned HTTP " + statusCode;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = firstNonBlank(
                    textValue(root, "message"),
                    nestedTextValue(root, "error", "message"),
                    textValue(root, "error")
            );
            if (StringUtils.hasText(message)) {
                return "OpenAI-compatible LLM API error (" + statusCode + "): " + message;
            }
        } catch (IOException ignored) {
            // Fall back to raw response body below.
        }
        return "OpenAI-compatible LLM API error (" + statusCode + "): " + responseBody;
    }

    private String nestedTextValue(JsonNode root, String objectField, String nestedField) {
        JsonNode objectNode = root.get(objectField);
        if (objectNode == null || objectNode.isNull()) {
            return null;
        }
        return textValue(objectNode, nestedField);
    }

    private String textValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node == null || node.isNull() ? null : node.asText();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static HttpClient buildHttpClient(AssistantProperties assistantProperties) {
        Duration connectTimeout = assistantProperties.getLlm().getOpenaiCompatible().getConnectTimeout();
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }
}
