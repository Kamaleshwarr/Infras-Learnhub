package com.company.learninghub.assistant.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockLlmClientTest {

    private final MockLlmClient mockLlmClient = new MockLlmClient();

    @Test
    void completeReturnsCertificateSubmissionGuidance() {
        LlmCompletionResult result = mockLlmClient.complete(MockLlmClient.knowledgeRequest(
                "How do I submit a certificate?"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("submit a certificate");
        assertThat(result.content()).contains("/submissions/new");
        assertThat(result.providerReference()).isEqualTo("mock-mode");
    }

    @Test
    void completeReturnsSpringBootExplanation() {
        LlmCompletionResult result = mockLlmClient.complete(MockLlmClient.knowledgeRequest(
                "What is Spring Boot?"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.content()).containsIgnoringCase("spring boot");
    }

    @Test
    void completeReturnsUnknownFallbackForUnrecognizedQuestions() {
        LlmCompletionResult result = mockLlmClient.complete(MockLlmClient.knowledgeRequest(
                "xyzzy plugh"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("don't currently have enough information");
    }

    @Test
    void reportsHealthyAndProviderName() {
        assertThat(mockLlmClient.isHealthy()).isTrue();
        assertThat(mockLlmClient.providerName()).isEqualTo("mock");
    }
}
