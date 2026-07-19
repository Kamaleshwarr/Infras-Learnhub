package com.company.learninghub.communication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.communication")
public class CommunicationProperties {

    private boolean enabled = true;
    private String supportEmail = "support@learninghub.local";
    private String frontendBaseUrl = "http://localhost:5173";
    private Email email = new Email();
    private Outbox outbox = new Outbox();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public void setOutbox(Outbox outbox) {
        this.outbox = outbox;
    }

    public static class Email {
        /**
         * Supported providers: log (development), resend (production), or smtp (legacy).
         */
        private String provider = "log";
        private String from = "noreply@learninghub.local";
        private Resend resend = new Resend();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public Resend getResend() {
            return resend;
        }

        public void setResend(Resend resend) {
            this.resend = resend;
        }

        public boolean isLogMode() {
            return provider == null || provider.equalsIgnoreCase("log");
        }

        public boolean isResendMode() {
            return "resend".equalsIgnoreCase(provider);
        }

        public boolean isSmtpMode() {
            return "smtp".equalsIgnoreCase(provider);
        }
    }

    public static class Resend {
        private String apiKey = "";
        private String baseUrl = "https://api.resend.com";
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }
    }

    public static class Outbox {
        private Duration pollInterval = Duration.ofSeconds(10);
        private int batchSize = 20;
        private int maxRetries = 3;
        private List<Duration> backoff = List.of(
                Duration.ofMinutes(1),
                Duration.ofMinutes(5),
                Duration.ofMinutes(15)
        );

        public Duration getPollInterval() {
            return pollInterval;
        }

        public void setPollInterval(Duration pollInterval) {
            this.pollInterval = pollInterval;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public List<Duration> getBackoff() {
            return backoff;
        }

        public void setBackoff(List<Duration> backoff) {
            this.backoff = backoff;
        }

        public Duration backoffForAttempt(int retryCount) {
            if (backoff == null || backoff.isEmpty()) {
                return Duration.ofMinutes(1);
            }
            int index = Math.min(Math.max(retryCount - 1, 0), backoff.size() - 1);
            return backoff.get(index);
        }
    }
}
