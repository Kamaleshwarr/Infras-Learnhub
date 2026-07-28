package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.tool.ToolResult;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.user.domain.RoleName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptOrchestratorTest {

    private PromptOrchestrator promptOrchestrator;

    @BeforeEach
    void setUp() {
        promptOrchestrator = new PromptOrchestrator(new ObjectMapper());
    }

    @Test
    void buildKnowledgeRequestIncludesSystemInstructionsAndHistory() {
        List<AssistantMessage> history = List.of(
                assistantMessage(AssistantMessageRole.USER, "hello"),
                assistantMessage(AssistantMessageRole.ASSISTANT, "Hi there")
        );

        LlmCompletionRequest request = promptOrchestrator.buildKnowledgeRequest("what is docker", history);

        assertThat(request.systemPrompt()).contains("Engineering Learning Hub assistant");
        assertThat(request.systemPrompt()).contains("Never invent user-specific platform data");
        assertThat(request.messages()).hasSize(3);
        assertThat(request.messages().get(0).role()).isEqualTo("user");
        assertThat(request.messages().get(0).content()).isEqualTo("hello");
        assertThat(request.messages().get(2).content()).isEqualTo("what is docker");
    }

    @Test
    void buildToolGroundedRequestEmbedsAuthoritativeToolData() {
        ToolResult toolResult = ToolResult.structured(
                "Profile for Employee (employee@learninghub.local).",
                new ProfileResponse(
                        UUID.randomUUID(),
                        "EMP001",
                        "Employee",
                        "employee@learninghub.local",
                        RoleName.EMPLOYEE,
                        true,
                        false,
                        false,
                        null,
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-01T00:00:00Z")
                )
        );

        LlmCompletionRequest request = promptOrchestrator.buildToolGroundedRequest(
                "my profile",
                "my-profile",
                toolResult,
                List.of()
        );

        assertThat(request.systemPrompt()).contains("Use only the authoritative tool data provided below");
        assertThat(request.messages()).hasSize(1);
        assertThat(request.messages().getFirst().content()).contains(PromptOrchestrator.TOOL_CONTEXT_MARKER);
        assertThat(request.messages().getFirst().content()).contains("my-profile");
        assertThat(request.messages().getFirst().content()).contains("Profile for Employee");
        assertThat(request.messages().getFirst().content()).contains("employee@learninghub.local");
    }

    @Test
    void extractToolSummaryReturnsGroundedSummary() {
        String content = """
                User question: my profile

                %s
                Tool name: my-profile
                Summary: Profile for Employee (employee@learninghub.local).
                Structured data: {"fullName":"Employee"}
                """.formatted(PromptOrchestrator.TOOL_CONTEXT_MARKER);

        assertThat(PromptOrchestrator.extractToolSummary(content))
                .isEqualTo("Profile for Employee (employee@learninghub.local).");
    }

    private AssistantMessage assistantMessage(AssistantMessageRole role, String content) {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        AssistantConversation conversation = new AssistantConversation(
                user,
                Instant.parse("2026-07-28T10:00:00Z"),
                Instant.parse("2026-07-28T10:00:00Z")
        );
        return new AssistantMessage(conversation, role, content, Instant.parse("2026-07-28T10:00:00Z"));
    }
}
