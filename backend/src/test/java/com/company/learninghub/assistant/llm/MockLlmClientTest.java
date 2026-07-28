package com.company.learninghub.assistant.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockLlmClientTest {

    private final MockLlmClient mockLlmClient = new MockLlmClient();

    @Test
    void completeReturnsMockResponse() {
        LlmCompletionResult result = mockLlmClient.complete(new LlmCompletionRequest(
                "system",
                java.util.List.of(new LlmCompletionRequest.LlmMessage("user", "hello"))
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("Mock LLM response");
        assertThat(result.providerReference()).isEqualTo("mock-mode");
    }

    @Test
    void reportsHealthyAndProviderName() {
        assertThat(mockLlmClient.isHealthy()).isTrue();
        assertThat(mockLlmClient.providerName()).isEqualTo("mock");
    }
}
