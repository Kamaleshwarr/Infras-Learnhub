package com.company.learninghub.assistant.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockLlmClient implements LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockLlmClient.class);

    @Override
    public LlmCompletionResult complete(LlmCompletionRequest request) {
        LOGGER.debug(
                "Assistant LLM (mock mode). systemPromptLength={}, messageCount={}",
                request.systemPrompt() == null ? 0 : request.systemPrompt().length(),
                request.messages() == null ? 0 : request.messages().size()
        );
        return LlmCompletionResult.success(
                "Mock LLM response — assistant orchestration is not yet implemented.",
                "mock-mode"
        );
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public String providerName() {
        return "mock";
    }
}
