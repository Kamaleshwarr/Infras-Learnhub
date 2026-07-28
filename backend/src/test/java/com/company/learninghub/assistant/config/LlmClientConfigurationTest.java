package com.company.learninghub.assistant.config;

import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.llm.MockLlmClient;
import com.company.learninghub.assistant.llm.OpenAiCompatibleClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmClientConfigurationTest {

    private final LlmClientConfiguration configuration = new LlmClientConfiguration();
    private final AssistantProperties assistantProperties = new AssistantProperties();
    private final MockLlmClient mockLlmClient = new MockLlmClient();
    private final OpenAiCompatibleClient openAiCompatibleClient = new OpenAiCompatibleClient(assistantProperties);

    @Test
    void selectsMockProviderByDefault() {
        LlmClient llmClient = configuration.llmClient(
                assistantProperties,
                mockLlmClient,
                openAiCompatibleClient
        );

        assertThat(llmClient).isSameAs(mockLlmClient);
        assertThat(llmClient.providerName()).isEqualTo("mock");
    }

    @Test
    void selectsOpenAiCompatibleProviderWhenConfigured() {
        assistantProperties.getLlm().setProvider("openai-compatible");

        LlmClient llmClient = configuration.llmClient(
                assistantProperties,
                mockLlmClient,
                openAiCompatibleClient
        );

        assertThat(llmClient).isSameAs(openAiCompatibleClient);
        assertThat(llmClient.providerName()).isEqualTo("openai-compatible");
    }

    @Test
    void fallsBackToMockProviderForUnknownValue() {
        assistantProperties.getLlm().setProvider("unknown");

        LlmClient llmClient = configuration.llmClient(
                assistantProperties,
                mockLlmClient,
                openAiCompatibleClient
        );

        assertThat(llmClient).isSameAs(mockLlmClient);
    }
}
