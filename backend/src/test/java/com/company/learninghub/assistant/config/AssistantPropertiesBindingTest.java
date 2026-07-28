package com.company.learninghub.assistant.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantPropertiesBindingTest {

    @Test
    void bindsAssistantPropertiesFromEnvironmentStyleKeys() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "app.assistant.enabled", "false",
                "app.assistant.llm.provider", "openai-compatible",
                "app.assistant.llm.openai-compatible.api-key", "test-key",
                "app.assistant.llm.openai-compatible.base-url", "https://example.com",
                "app.assistant.llm.openai-compatible.model", "gpt-test"
        ));

        AssistantProperties properties = new Binder(source)
                .bind("app.assistant", AssistantProperties.class)
                .get();

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getLlm().getProvider()).isEqualTo("openai-compatible");
        assertThat(properties.getLlm().getOpenaiCompatible().getApiKey()).isEqualTo("test-key");
        assertThat(properties.getLlm().getOpenaiCompatible().getBaseUrl()).isEqualTo("https://example.com");
        assertThat(properties.getLlm().getOpenaiCompatible().getModel()).isEqualTo("gpt-test");
    }
}
