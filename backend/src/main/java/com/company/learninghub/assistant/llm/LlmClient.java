package com.company.learninghub.assistant.llm;

public interface LlmClient {

    LlmCompletionResult complete(LlmCompletionRequest request);

    boolean isHealthy();

    String providerName();
}
