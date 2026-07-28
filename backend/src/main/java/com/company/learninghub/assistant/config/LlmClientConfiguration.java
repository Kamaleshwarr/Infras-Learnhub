package com.company.learninghub.assistant.config;

import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.llm.MockLlmClient;
import com.company.learninghub.assistant.llm.OpenAiCompatibleClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LlmClientConfiguration {

    @Bean
    @Primary
    public LlmClient llmClient(
            AssistantProperties assistantProperties,
            MockLlmClient mockLlmClient,
            OpenAiCompatibleClient openAiCompatibleClient
    ) {
        if (assistantProperties.getLlm().isMockMode()) {
            return mockLlmClient;
        }
        if (assistantProperties.getLlm().isOpenAiCompatibleMode()) {
            return openAiCompatibleClient;
        }
        return mockLlmClient;
    }
}
