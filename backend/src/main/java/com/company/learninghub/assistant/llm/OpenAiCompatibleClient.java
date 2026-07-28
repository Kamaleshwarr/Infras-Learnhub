package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.config.AssistantProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Skeleton implementation for OpenAI-compatible chat completion APIs.
 * Full HTTP integration will be added in a later phase when orchestration is implemented.
 */
@Component
public class OpenAiCompatibleClient implements LlmClient {

    private final AssistantProperties assistantProperties;

    public OpenAiCompatibleClient(AssistantProperties assistantProperties) {
        this.assistantProperties = assistantProperties;
    }

    @Override
    public LlmCompletionResult complete(LlmCompletionRequest request) {
        return LlmCompletionResult.failure(
                "OpenAI-compatible LLM integration is not yet implemented in Phase 1 foundation."
        );
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
}
