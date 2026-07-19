package com.company.learninghub.communication.email;

import com.company.learninghub.communication.config.CommunicationProperties;
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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

class ResendEmailProviderTest {

    private HttpServer server;
    private String lastAuthorization;
    private String lastRequestBody;
    private int responseStatus = 200;
    private String responseBody = "{\"id\":\"email_123\"}";
    private ResendEmailProvider provider;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/emails", this::handleEmails);
        server.start();

        CommunicationProperties properties = new CommunicationProperties();
        properties.getEmail().setProvider("resend");
        properties.getEmail().getResend().setApiKey("re_test_key");
        properties.getEmail().getResend().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.getEmail().getResend().setConnectTimeout(Duration.ofSeconds(2));
        properties.getEmail().getResend().setReadTimeout(Duration.ofSeconds(2));

        provider = new ResendEmailProvider(properties, new ObjectMapper(), HttpClient.newHttpClient());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendReturnsSuccessForSuccessfulApiResponse() {
        EmailDeliveryResult result = provider.send(sampleMessage());

        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).isEqualTo("email_123");
        assertThat(lastAuthorization).isEqualTo("Bearer re_test_key");
        assertThat(lastRequestBody).contains("\"subject\":\"Certificate approved\"");
        assertThat(lastRequestBody).contains("\"text\":\"Plain text body\"");
        assertThat(lastRequestBody).contains("\"html\":\"<p>HTML body</p>\"");
        assertThat(provider.providerName()).isEqualTo("resend");
        assertThat(provider.isHealthy()).isTrue();
    }

    @Test
    void sendReturnsFailureForApiErrorResponse() {
        responseStatus = 422;
        responseBody = "{\"statusCode\":422,\"name\":\"validation_error\",\"message\":\"Invalid from address\"}";

        EmailDeliveryResult result = provider.send(sampleMessage());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("422");
        assertThat(result.errorMessage()).contains("Invalid from address");
    }

    @Test
    void sendReturnsFailureWhenApiKeyMissing() {
        CommunicationProperties properties = new CommunicationProperties();
        properties.getEmail().getResend().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        ResendEmailProvider missingKeyProvider = new ResendEmailProvider(
                properties,
                new ObjectMapper(),
                HttpClient.newHttpClient()
        );

        EmailDeliveryResult result = missingKeyProvider.send(sampleMessage());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("API key is not configured");
        assertThat(missingKeyProvider.isHealthy()).isFalse();
    }

    @Test
    void sendReturnsFailureForTimeout() {
        responseStatus = 200;
        server.removeContext("/emails");
        server.createContext("/emails", exchange -> {
            try {
                Thread.sleep(2500);
                writeResponse(exchange, 200, "{\"id\":\"late\"}");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        EmailDeliveryResult result = provider.send(sampleMessage());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("timed out");
    }

    private void handleEmails(HttpExchange exchange) throws IOException {
        lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
        lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        writeResponse(exchange, responseStatus, responseBody);
    }

    private void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private EmailMessage sampleMessage() {
        return new EmailMessage(
                "noreply@learninghub.local",
                "employee@learninghub.local",
                "Certificate approved",
                "Plain text body",
                "<p>HTML body</p>"
        );
    }
}
