package com.company.learninghub.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.assistant")
public class AssistantProperties {

    private boolean enabled = false;
    private Llm llm = new Llm();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Llm getLlm() {
        return llm;
    }

    public void setLlm(Llm llm) {
        this.llm = llm;
    }

    public static class Llm {
        /**
         * Supported providers: mock (development) or openai-compatible (production).
         */
        private String provider = "mock";
        private OpenAiCompatible openaiCompatible = new OpenAiCompatible();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public OpenAiCompatible getOpenaiCompatible() {
            return openaiCompatible;
        }

        public void setOpenaiCompatible(OpenAiCompatible openaiCompatible) {
            this.openaiCompatible = openaiCompatible;
        }

        public boolean isMockMode() {
            return provider == null || provider.equalsIgnoreCase("mock");
        }

        public boolean isOpenAiCompatibleMode() {
            return "openai-compatible".equalsIgnoreCase(provider);
        }
    }

    public static class OpenAiCompatible {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com";
        private String model = "gpt-4o-mini";
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(60);

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

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
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
}
