package com.company.learninghub.communication.email;

import com.company.learninghub.communication.config.CommunicationProperties;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResendEmailProvider implements EmailProvider {

    private static final String EMAILS_PATH = "/emails";

    private final CommunicationProperties communicationProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public ResendEmailProvider(CommunicationProperties communicationProperties, ObjectMapper objectMapper) {
        this(communicationProperties, objectMapper, buildHttpClient(communicationProperties));
    }

    ResendEmailProvider(
            CommunicationProperties communicationProperties,
            ObjectMapper objectMapper,
            HttpClient httpClient
    ) {
        this.communicationProperties = communicationProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public EmailDeliveryResult send(EmailMessage message) {
        CommunicationProperties.Resend resend = communicationProperties.getEmail().getResend();
        if (!StringUtils.hasText(resend.getApiKey())) {
            return EmailDeliveryResult.failure("Resend API key is not configured");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestBody(message));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(resolveEmailsUri(resend.getBaseUrl()))
                    .timeout(resend.getReadTimeout())
                    .header("Authorization", "Bearer " + resend.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return EmailDeliveryResult.success(extractMessageId(response.body()));
            }
            return EmailDeliveryResult.failure(extractErrorMessage(response.statusCode(), response.body()));
        } catch (HttpTimeoutException ex) {
            return EmailDeliveryResult.failure("Resend request timed out");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return EmailDeliveryResult.failure("Resend request interrupted");
        } catch (IOException ex) {
            return EmailDeliveryResult.failure("Resend request failed: " + ex.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        return StringUtils.hasText(communicationProperties.getEmail().getResend().getApiKey());
    }

    @Override
    public String providerName() {
        return "resend";
    }

    private Map<String, Object> buildRequestBody(EmailMessage message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", message.from());
        body.put("to", List.of(message.to()));
        body.put("subject", message.subject());
        body.put("text", message.textBody());
        if (StringUtils.hasText(message.htmlBody())) {
            body.put("html", message.htmlBody());
        }
        return body;
    }

    private URI resolveEmailsUri(String baseUrl) {
        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return URI.create(normalizedBase + EMAILS_PATH);
    }

    private String extractMessageId(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode idNode = root.get("id");
        if (idNode == null || !StringUtils.hasText(idNode.asText())) {
            return "resend-sent";
        }
        return idNode.asText();
    }

    private String extractErrorMessage(int statusCode, String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "Resend API returned HTTP " + statusCode;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = firstNonBlank(
                    textValue(root, "message"),
                    textValue(root, "error")
            );
            if (StringUtils.hasText(message)) {
                return "Resend API error (" + statusCode + "): " + message;
            }
        } catch (IOException ignored) {
            // Fall back to raw response body below.
        }
        return "Resend API error (" + statusCode + "): " + responseBody;
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

    private static HttpClient buildHttpClient(CommunicationProperties communicationProperties) {
        Duration connectTimeout = communicationProperties.getEmail().getResend().getConnectTimeout();
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }
}
