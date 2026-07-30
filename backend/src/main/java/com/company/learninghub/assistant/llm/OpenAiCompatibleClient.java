package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.diagnostics.AssistantChatDiagnostics;
import com.company.learninghub.assistant.dto.AssistantLlmDebugResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleClient.class);
    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final int RESPONSE_BODY_LOG_LIMIT = 500;
    private static final int DIAGNOSTIC_RESPONSE_LOG_LIMIT = 1000;
    private static final Duration OLLAMA_MINIMUM_READ_TIMEOUT = Duration.ofSeconds(180);
    private static final String DEBUG_PROBE_PROMPT = "Reply with exactly:\n\nHELLO_FROM_OLLAMA";

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
    public String providerName() {
        return "openai-compatible";
    }

    /**
     * Temporary diagnostic probe that bypasses orchestration and calls the LLM directly.
     */
    public AssistantLlmDebugResponse probeDirectLlmCall() {
        LlmCompletionRequest request = new LlmCompletionRequest(
                null,
                List.of(new LlmCompletionRequest.LlmMessage("user", DEBUG_PROBE_PROMPT))
        );
        DiagnosticExecution execution = executeHttpRequest(request);
        ParsedMessageFields fields = execution.parsedFields() == null
                ? ParsedMessageFields.empty()
                : execution.parsedFields();
        return new AssistantLlmDebugResponse(
                providerName(),
                execution.elapsedMs(),
                execution.result().success(),
                execution.httpStatus(),
                execution.requestUri() == null ? null : execution.requestUri().toString(),
                AssistantChatDiagnostics.truncate(execution.rawResponse(), DIAGNOSTIC_RESPONSE_LOG_LIMIT),
                fields.content(),
                fields.reasoning(),
                execution.result().success() ? execution.result().content() : null,
                execution.result().success() ? null : execution.result().errorMessage()
        );
    }

    @Override
    public LlmCompletionResult complete(LlmCompletionRequest request) {
        DiagnosticExecution execution = executeHttpRequest(request);
        recordDiagnosticTrace(execution);
        return execution.result();
    }

    @Override
    public boolean isHealthy() {
        AssistantProperties.OpenAiCompatible config = assistantProperties.getLlm().getOpenaiCompatible();
        return StringUtils.hasText(config.getBaseUrl()) && StringUtils.hasText(config.getModel());
    }

    private void recordDiagnosticTrace(DiagnosticExecution execution) {
        ParsedMessageFields fields = execution.parsedFields() == null
                ? ParsedMessageFields.empty()
                : execution.parsedFields();
        AssistantChatDiagnostics.recordLlmHttpEnd(
                execution.httpStatus() == null ? -1 : execution.httpStatus(),
                execution.elapsedMs(),
                AssistantChatDiagnostics.truncate(execution.rawResponse(), DIAGNOSTIC_RESPONSE_LOG_LIMIT),
                fields.content(),
                fields.reasoning(),
                fields.reasoningContent(),
                execution.result().success() ? null : execution.result().errorMessage(),
                execution.exception()
        );
    }

    private DiagnosticExecution executeHttpRequest(LlmCompletionRequest request) {
        AssistantProperties.OpenAiCompatible config = assistantProperties.getLlm().getOpenaiCompatible();
        if (!StringUtils.hasText(config.getBaseUrl())) {
            return DiagnosticExecution.failure(
                    URI.create(""),
                    0L,
                    null,
                    null,
                    LlmCompletionResult.failure("OpenAI-compatible base URL is not configured"),
                    null
            );
        }
        if (!StringUtils.hasText(config.getModel())) {
            return DiagnosticExecution.failure(
                    resolveChatCompletionsUri(config.getBaseUrl()),
                    0L,
                    null,
                    null,
                    LlmCompletionResult.failure("OpenAI-compatible model is not configured"),
                    null
            );
        }

        URI requestUri = resolveChatCompletionsUri(config.getBaseUrl());
        Duration readTimeout = effectiveReadTimeout(config);
        long startedAtNanos = System.nanoTime();
        AssistantChatDiagnostics.current().ifPresent(ignored -> AssistantChatDiagnostics.recordLlmHttpStart(requestUri.toString()));
        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestBody(request, config));
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(requestUri)
                    .timeout(readTimeout)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            if (StringUtils.hasText(config.getApiKey())) {
                requestBuilder.header("Authorization", "Bearer " + config.getApiKey().trim());
            }

            log.debug(
                    "OpenAI-compatible LLM request starting: uri={}, model={}, configuredReadTimeout={}, effectiveReadTimeout={}, requestBody={}",
                    requestUri,
                    config.getModel(),
                    config.getReadTimeout(),
                    readTimeout,
                    truncateForLog(requestBody)
            );

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            long elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            String responseBody = response.body();
            log.debug(
                    "OpenAI-compatible LLM response: status={}, elapsedMs={}, body={}",
                    response.statusCode(),
                    elapsedMs,
                    truncateForLog(responseBody)
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseSuccessResponse(requestUri, elapsedMs, response.statusCode(), responseBody);
            }
            String errorMessage = extractErrorMessage(response.statusCode(), responseBody);
            log.warn(
                    "OpenAI-compatible LLM request failed: uri={}, status={}, elapsedMs={}, error={}",
                    requestUri,
                    response.statusCode(),
                    elapsedMs,
                    errorMessage
            );
            return DiagnosticExecution.failure(
                    requestUri,
                    elapsedMs,
                    response.statusCode(),
                    responseBody,
                    LlmCompletionResult.failure(errorMessage),
                    null
            );
        } catch (HttpTimeoutException ex) {
            long elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            log.warn(
                    "OpenAI-compatible LLM request timed out: uri={}, configuredReadTimeout={}, effectiveReadTimeout={}, elapsedMs={}",
                    requestUri,
                    config.getReadTimeout(),
                    readTimeout,
                    elapsedMs,
                    ex
            );
            return DiagnosticExecution.failure(
                    requestUri,
                    elapsedMs,
                    null,
                    null,
                    LlmCompletionResult.failure("OpenAI-compatible request timed out"),
                    ex
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            long elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            log.warn("OpenAI-compatible LLM request interrupted: uri={}", requestUri, ex);
            return DiagnosticExecution.failure(
                    requestUri,
                    elapsedMs,
                    null,
                    null,
                    LlmCompletionResult.failure("OpenAI-compatible request interrupted"),
                    ex
            );
        } catch (IOException ex) {
            long elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            log.warn(
                    "OpenAI-compatible LLM request failed: uri={}, elapsedMs={}, message={}",
                    requestUri,
                    elapsedMs,
                    ex.getMessage(),
                    ex
            );
            return DiagnosticExecution.failure(
                    requestUri,
                    elapsedMs,
                    null,
                    null,
                    LlmCompletionResult.failure("OpenAI-compatible request failed: " + ex.getMessage()),
                    ex
            );
        }
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
        if (isOllamaCompatibleEndpoint(config.getBaseUrl())) {
            body.put("reasoning_effort", "none");
        }
        return body;
    }

    private DiagnosticExecution parseSuccessResponse(
            URI requestUri,
            long elapsedMs,
            int httpStatus,
            String responseBody
    ) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return DiagnosticExecution.failure(
                    requestUri,
                    elapsedMs,
                    httpStatus,
                    responseBody,
                    LlmCompletionResult.failure("OpenAI-compatible response did not include any choices"),
                    null
            );
        }

        JsonNode messageNode = choices.get(0).path("message");
        ParsedMessageFields fields = ParsedMessageFields.from(messageNode, this::textValue);
        log.debug(
                "OpenAI-compatible parsed message fields: content={}, reasoning={}, reasoning_content={}",
                truncateForLog(fields.content()),
                truncateForLog(fields.reasoning()),
                truncateForLog(fields.reasoningContent())
        );

        String content = extractMessageText(fields);
        if (!StringUtils.hasText(content)) {
            log.warn(
                    "OpenAI-compatible response did not include usable message content: body={}",
                    truncateForLog(responseBody)
            );
            return DiagnosticExecution.failure(
                    requestUri,
                    elapsedMs,
                    httpStatus,
                    responseBody,
                    LlmCompletionResult.failure("OpenAI-compatible response did not include message content"),
                    null,
                    fields
            );
        }

        String providerReference = root.path("id").asText("openai-compatible");
        log.debug("OpenAI-compatible parsed response content length={}", content.length());
        return new DiagnosticExecution(
                requestUri,
                elapsedMs,
                httpStatus,
                responseBody,
                fields,
                LlmCompletionResult.success(content.trim(), providerReference),
                null
        );
    }

    private String extractMessageText(ParsedMessageFields fields) {
        if (StringUtils.hasText(fields.content())) {
            return fields.content();
        }
        if (StringUtils.hasText(fields.reasoning())) {
            log.info("Using Ollama reasoning field because message.content was empty");
            return fields.reasoning();
        }
        return fields.reasoningContent();
    }

    static boolean isOllamaCompatibleEndpoint(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return false;
        }
        String normalized = baseUrl.trim().toLowerCase();
        return normalized.contains("localhost")
                || normalized.contains("127.0.0.1")
                || normalized.contains("host.docker.internal")
                || normalized.contains(":11434");
    }

    private static String truncateForLog(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= RESPONSE_BODY_LOG_LIMIT) {
            return value;
        }
        return value.substring(0, RESPONSE_BODY_LOG_LIMIT) + "...";
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
        return URI.create(normalizeBaseUrl(baseUrl) + CHAT_COMPLETIONS_PATH);
    }

    static String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String normalizedBase = baseUrl.trim();
        while (normalizedBase.endsWith("/")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        if (normalizedBase.endsWith("/v1")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 3);
        }
        return normalizedBase;
    }

    private Duration effectiveReadTimeout(AssistantProperties.OpenAiCompatible config) {
        Duration configured = config.getReadTimeout() == null
                ? Duration.ofSeconds(60)
                : config.getReadTimeout();
        if (requiresExtendedOllamaReadTimeout(config.getBaseUrl()) && configured.compareTo(OLLAMA_MINIMUM_READ_TIMEOUT) < 0) {
            log.debug(
                    "Applying Ollama minimum read timeout {} (configured={})",
                    OLLAMA_MINIMUM_READ_TIMEOUT,
                    configured
            );
            return OLLAMA_MINIMUM_READ_TIMEOUT;
        }
        return configured;
    }

    static boolean requiresExtendedOllamaReadTimeout(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return false;
        }
        String normalized = baseUrl.trim().toLowerCase();
        return normalized.contains("host.docker.internal") || normalized.contains(":11434");
    }

    private static HttpClient buildHttpClient(AssistantProperties assistantProperties) {
        Duration connectTimeout = assistantProperties.getLlm().getOpenaiCompatible().getConnectTimeout();
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }

    private record ParsedMessageFields(String content, String reasoning, String reasoningContent) {
        static ParsedMessageFields empty() {
            return new ParsedMessageFields(null, null, null);
        }

        static ParsedMessageFields from(JsonNode messageNode, java.util.function.BiFunction<JsonNode, String, String> textValue) {
            return new ParsedMessageFields(
                    textValue.apply(messageNode, "content"),
                    textValue.apply(messageNode, "reasoning"),
                    textValue.apply(messageNode, "reasoning_content")
            );
        }
    }

    private record DiagnosticExecution(
            URI requestUri,
            long elapsedMs,
            Integer httpStatus,
            String rawResponse,
            ParsedMessageFields parsedFields,
            LlmCompletionResult result,
            Exception exception
    ) {
        static DiagnosticExecution failure(
                URI requestUri,
                long elapsedMs,
                Integer httpStatus,
                String rawResponse,
                LlmCompletionResult result,
                Exception exception
        ) {
            return new DiagnosticExecution(requestUri, elapsedMs, httpStatus, rawResponse, null, result, exception);
        }

        static DiagnosticExecution failure(
                URI requestUri,
                long elapsedMs,
                Integer httpStatus,
                String rawResponse,
                LlmCompletionResult result,
                Exception exception,
                ParsedMessageFields parsedFields
        ) {
            return new DiagnosticExecution(requestUri, elapsedMs, httpStatus, rawResponse, parsedFields, result, exception);
        }
    }
}
