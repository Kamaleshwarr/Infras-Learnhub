package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.dto.AssistantSourceConfidence;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.tool.ToolResult;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptOrchestratorTest {

    private final PromptOrchestrator promptOrchestrator = new PromptOrchestrator(new ObjectMapper());

    @Test
    void buildKnowledgeRequestIncludesHistoryAndHallucinationGuard() {
        LlmCompletionRequest request = promptOrchestrator.buildKnowledgeRequest(
                "What is Docker?",
                sampleHistory()
        );

        assertThat(request.systemPrompt()).contains("Never invent user-specific platform data");
        assertThat(request.messages()).hasSize(3);
        assertThat(request.messages().get(0).role()).isEqualTo("user");
        assertThat(request.messages().get(0).content()).isEqualTo("hello");
        assertThat(request.messages().get(2).content()).isEqualTo("What is Docker?");
    }

    @Test
    void buildUnknownRequestUsesUnknownPrompt() {
        LlmCompletionRequest request = promptOrchestrator.buildUnknownRequest("???", List.of());

        assertThat(request.systemPrompt()).contains("outside your scope");
        assertThat(request.messages()).containsExactly(
                new LlmCompletionRequest.LlmMessage("user", "???")
        );
    }

    @Test
    void buildToolGroundedRequestIncludesAuthoritativeToolData() {
        ToolResult toolResult = ToolResult.structured(
                "Rank 3 of 10",
                Map.of("rank", 3, "totalParticipants", 10)
        );

        LlmCompletionRequest request = promptOrchestrator.buildToolGroundedRequest(
                "my leaderboard rank",
                "my-leaderboard-rank",
                "LeaderboardService",
                AssistantSourceConfidence.HIGH,
                toolResult,
                List.of()
        );

        assertThat(request.systemPrompt()).contains("authoritative");
        assertThat(request.systemPrompt()).contains("my-leaderboard-rank");
        assertThat(request.systemPrompt()).contains("LeaderboardService");
        assertThat(request.systemPrompt()).contains("Rank 3 of 10");
        assertThat(request.systemPrompt()).contains("\"rank\" : 3");
        assertThat(request.messages()).containsExactly(
                new LlmCompletionRequest.LlmMessage("user", "my leaderboard rank")
        );
    }

    @Test
    void resolveContextTypeMapsIntentTypes() {
        assertThat(promptOrchestrator.resolveContextType(AssistantIntentType.KNOWLEDGE))
                .isEqualTo(PromptContextType.KNOWLEDGE);
        assertThat(promptOrchestrator.resolveContextType(AssistantIntentType.TOOL))
                .isEqualTo(PromptContextType.TOOL);
        assertThat(promptOrchestrator.resolveContextType(AssistantIntentType.UNKNOWN))
                .isEqualTo(PromptContextType.UNKNOWN);
    }

    private List<AssistantMessage> sampleHistory() {
        AssistantConversation conversation = new AssistantConversation(
                new User("EMP001", "employee@learninghub.local", "Employee", "hash"),
                Instant.parse("2026-07-28T10:00:00Z"),
                Instant.parse("2026-07-28T10:00:00Z")
        );
        return List.of(
                new AssistantMessage(conversation, AssistantMessageRole.USER, "hello", Instant.parse("2026-07-28T10:00:00Z")),
                new AssistantMessage(conversation, AssistantMessageRole.ASSISTANT, "Hi there", Instant.parse("2026-07-28T10:01:00Z"))
        );
    }
}
