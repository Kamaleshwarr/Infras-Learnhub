package com.company.learninghub.assistant.llm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockLlmClientTest {

    private final MockLlmClient mockLlmClient = new MockLlmClient();
    private final PromptOrchestrator promptOrchestrator = new PromptOrchestrator(
            new com.fasterxml.jackson.databind.ObjectMapper()
    );

    @Test
    void completeReturnsCertificateSubmissionGuidance() {
        LlmCompletionResult result = mockLlmClient.complete(
                promptOrchestrator.buildKnowledgeRequest("How do I submit a certificate?", List.of())
        );

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("submit a certificate");
        assertThat(result.content()).contains("/submissions/new");
        assertThat(result.providerReference()).isEqualTo("mock-mode");
    }

    @Test
    void completeReturnsSpringBootExplanation() {
        LlmCompletionResult result = mockLlmClient.complete(
                promptOrchestrator.buildKnowledgeRequest("What is Spring Boot?", List.of())
        );

        assertThat(result.success()).isTrue();
        assertThat(result.content()).containsIgnoringCase("spring boot");
    }

    @Test
    void completeReturnsUnknownFallbackForUnrecognizedQuestions() {
        LlmCompletionResult result = mockLlmClient.complete(
                promptOrchestrator.buildKnowledgeRequest("xyzzy plugh", List.of())
        );

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("don't currently have enough information");
    }

    @Test
    void completeReturnsGroundedToolSummary() {
        LlmCompletionRequest request = promptOrchestrator.buildToolGroundedRequest(
                "my profile",
                "my-profile",
                com.company.learninghub.assistant.tool.ToolResult.text("Profile for Employee (employee@learninghub.local)."),
                List.of()
        );

        LlmCompletionResult result = mockLlmClient.complete(request);

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("Profile for Employee");
        assertThat(result.providerReference()).isEqualTo("mock-mode-tool");
    }

    @Test
    void reportsHealthyAndProviderName() {
        assertThat(mockLlmClient.isHealthy()).isTrue();
        assertThat(mockLlmClient.providerName()).isEqualTo("mock");
    }
}
