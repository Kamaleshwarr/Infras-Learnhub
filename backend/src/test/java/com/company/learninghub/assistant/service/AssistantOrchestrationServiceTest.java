package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.dto.AssistantOrchestrationResponse;
import com.company.learninghub.assistant.dto.AssistantOutcomeType;
import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantResponse;
import com.company.learninghub.assistant.dto.AssistantSourceConfidence;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.dto.ConversationResponse;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.IntentResolver;
import com.company.learninghub.assistant.intent.NavigationTarget;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.llm.LlmCompletionResult;
import com.company.learninghub.assistant.llm.PromptOrchestrator;
import com.company.learninghub.assistant.tool.AssistantToolContext;
import com.company.learninghub.assistant.tool.AssistantToolNames;
import com.company.learninghub.assistant.tool.AssistantToolRegistry;
import com.company.learninghub.assistant.tool.ToolResult;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantOrchestrationServiceTest {

    @Mock
    private LlmClient llmClient;

    @Mock
    private PromptOrchestrator promptOrchestrator;

    @Mock
    private IntentResolver intentResolver;

    @Mock
    private AssistantToolRegistry toolRegistry;

    @Mock
    private AssistantConversationService conversationService;

    private AssistantProperties assistantProperties;
    private AssistantOrchestrationService service;
    private AuthenticatedUser authenticatedUser;
    private AssistantConversation conversation;

    @BeforeEach
    void setUp() {
        assistantProperties = new AssistantProperties();
        service = new AssistantOrchestrationService(
                assistantProperties,
                llmClient,
                promptOrchestrator,
                intentResolver,
                toolRegistry,
                conversationService
        );
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        authenticatedUser = AuthenticatedUser.from(user);
        conversation = new AssistantConversation(user, Instant.parse("2026-07-28T10:00:00Z"), Instant.parse("2026-07-28T10:00:00Z"));
    }

    @Test
    void getStatusReflectsFeatureFlagAndProviderHealth() {
        assistantProperties.setEnabled(false);

        when(llmClient.providerName()).thenReturn("mock");
        when(llmClient.isHealthy()).thenReturn(true);

        AssistantStatusResponse status = service.getStatus();

        assertThat(status.enabled()).isFalse();
        assertThat(status.llmProvider()).isEqualTo("mock");
        assertThat(status.llmHealthy()).isTrue();
    }

    @Test
    void processRequestReturnsDisabledWhenFeatureFlagOff() {
        assistantProperties.setEnabled(false);

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("my profile", null),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.DISABLED);
        verify(intentResolver, never()).resolve(any());
    }

    @Test
    void processRequestReturnsNavigationInstruction() {
        assistantProperties.setEnabled(true);
        when(intentResolver.resolve("open projects"))
                .thenReturn(ResolvedIntent.navigation(new NavigationTarget("/projects", "Projects"), "open projects"));

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("open projects", null),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.NAVIGATION);
        assertThat(response.intentType()).isEqualTo(AssistantIntentType.NAVIGATION);
        assertThat(response.navigation().path()).isEqualTo("/projects");
        verify(toolRegistry, never()).execute(any(), any());
    }

    @Test
    void processRequestExecutesTool() {
        assistantProperties.setEnabled(true);
        ResolvedIntent intent = ResolvedIntent.tool(AssistantToolNames.MY_PROFILE, "my profile");
        ToolResult toolResult = ToolResult.text("Profile ready");
        when(intentResolver.resolve("my profile")).thenReturn(intent);
        when(toolRegistry.execute(eq(intent), any(AssistantToolContext.class))).thenReturn(toolResult);
        when(promptOrchestrator.buildToolGroundedRequest(
                eq("my profile"),
                eq(AssistantToolNames.MY_PROFILE),
                eq(toolResult),
                any()
        )).thenReturn(new com.company.learninghub.assistant.llm.LlmCompletionRequest("system", List.of()));
        when(llmClient.complete(any())).thenReturn(LlmCompletionResult.success("Profile ready", "mock-mode-tool"));

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("my profile", null),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.TOOL);
        assertThat(response.toolResult()).isEqualTo(toolResult);
        assertThat(response.toolName()).isEqualTo(AssistantToolNames.MY_PROFILE);
    }

    @Test
    void processRequestUsesMockLlmForKnowledge() {
        assistantProperties.setEnabled(true);
        when(intentResolver.resolve("what is docker")).thenReturn(ResolvedIntent.knowledge("what is docker"));
        when(promptOrchestrator.buildKnowledgeRequest(eq("what is docker"), any())).thenReturn(
                new com.company.learninghub.assistant.llm.LlmCompletionRequest("system", List.of())
        );
        when(llmClient.complete(any())).thenReturn(LlmCompletionResult.success("Docker explanation", "mock-mode"));

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("what is docker", null),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.KNOWLEDGE);
        assertThat(response.message()).isEqualTo("Docker explanation");
        verify(llmClient).complete(any());
    }

    @Test
    void processRequestUsesMockLlmForUnknown() {
        assistantProperties.setEnabled(true);
        when(intentResolver.resolve("???")).thenReturn(ResolvedIntent.unknown(""));
        when(promptOrchestrator.buildKnowledgeRequest(eq("???"), any())).thenReturn(
                new com.company.learninghub.assistant.llm.LlmCompletionRequest("system", List.of())
        );
        when(llmClient.complete(any())).thenReturn(LlmCompletionResult.success(
                "I don't currently have enough information to answer this. Future versions will support broader AI knowledge.",
                "mock-mode"
        ));

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("???", null),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.UNKNOWN);
        assertThat(response.message()).contains("don't currently have enough information");
    }

    @Test
    void chatPersistsUserAndAssistantMessages() {
        assistantProperties.setEnabled(true);
        when(conversationService.resolveConversation(authenticatedUser, null)).thenReturn(conversation);
        when(intentResolver.resolve("my profile"))
                .thenReturn(ResolvedIntent.tool(AssistantToolNames.MY_PROFILE, "my profile"));
        when(toolRegistry.execute(any(), any(AssistantToolContext.class)))
                .thenReturn(ToolResult.text("Profile ready"));
        when(promptOrchestrator.buildToolGroundedRequest(
                eq("my profile"),
                eq(AssistantToolNames.MY_PROFILE),
                any(ToolResult.class),
                any()
        )).thenReturn(new com.company.learninghub.assistant.llm.LlmCompletionRequest("system", List.of()));
        when(llmClient.complete(any())).thenReturn(LlmCompletionResult.success("Profile ready", "mock-mode-tool"));
        when(conversationService.listMessages(authenticatedUser)).thenReturn(List.of());
        when(conversationService.appendMessage(eq(conversation), any(), any()))
                .thenAnswer(invocation -> new AssistantMessage(
                        conversation,
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        Instant.parse("2026-07-28T10:00:00Z")
                ));

        AssistantResponse response = service.chat(new AssistantRequest("my profile", null), authenticatedUser);

        assertThat(response.response()).isEqualTo("Profile ready");
        assertThat(response.conversationId()).isEqualTo(conversation.getId());
        assertThat(response.intentType()).isEqualTo(AssistantIntentType.TOOL);
        assertThat(response.toolUsed()).isEqualTo(AssistantToolNames.MY_PROFILE);
        assertThat(response.confidence()).isEqualTo(AssistantSourceConfidence.HIGH);
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().getFirst().confidence()).isEqualTo(AssistantSourceConfidence.HIGH);

        ArgumentCaptor<AssistantMessageRole> roleCaptor = ArgumentCaptor.forClass(AssistantMessageRole.class);
        verify(conversationService, times(2)).appendMessage(eq(conversation), roleCaptor.capture(), any());
        assertThat(roleCaptor.getAllValues()).containsExactly(
                AssistantMessageRole.USER,
                AssistantMessageRole.ASSISTANT
        );
    }

    @Test
    void chatThrowsWhenFeatureDisabled() {
        assistantProperties.setEnabled(false);

        assertThatThrownBy(() -> service.chat(new AssistantRequest("hello", null), authenticatedUser))
                .isInstanceOf(AssistantDisabledException.class);
    }

    @Test
    void processRequestIncludesToolGroundingMetadata() {
        assistantProperties.setEnabled(true);
        ToolResult toolResult = ToolResult.structured("Rank 3", Map.of("rank", 3));
        when(intentResolver.resolve("my leaderboard rank"))
                .thenReturn(ResolvedIntent.tool(AssistantToolNames.MY_LEADERBOARD_RANK, "my leaderboard rank"));
        when(toolRegistry.execute(any(), any(AssistantToolContext.class))).thenReturn(toolResult);
        when(promptOrchestrator.buildToolGroundedRequest(
                eq("my leaderboard rank"),
                eq(AssistantToolNames.MY_LEADERBOARD_RANK),
                eq(toolResult),
                any()
        )).thenReturn(new com.company.learninghub.assistant.llm.LlmCompletionRequest("system", List.of()));
        when(llmClient.complete(any())).thenReturn(LlmCompletionResult.success("Rank 3", "mock-mode-tool"));
        when(conversationService.resolveConversation(authenticatedUser, null)).thenReturn(conversation);
        when(conversationService.listMessages(authenticatedUser)).thenReturn(List.of());
        when(conversationService.appendMessage(eq(conversation), any(), any()))
                .thenAnswer(invocation -> new AssistantMessage(
                        conversation,
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        Instant.parse("2026-07-28T10:00:00Z")
                ));

        AssistantResponse response = service.chat(
                new AssistantRequest("my leaderboard rank", null),
                authenticatedUser
        );

        assertThat(response.toolUsed()).isEqualTo(AssistantToolNames.MY_LEADERBOARD_RANK);
        assertThat(response.confidence()).isEqualTo(AssistantSourceConfidence.HIGH);
        assertThat(response.metadata()).containsKey("grounding");
        assertThat(response.metadata().get("grounding")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> grounding = (Map<String, Object>) response.metadata().get("grounding");
        assertThat(grounding.get("authoritative")).isEqualTo(true);
        assertThat(grounding.get("summary")).isEqualTo("Rank 3");
    }

    @Test
    void getConversationRequiresFeatureEnabled() {
        assistantProperties.setEnabled(false);

        assertThatThrownBy(() -> service.getConversation(authenticatedUser))
                .isInstanceOf(AssistantDisabledException.class);
    }

    @Test
    void getConversationReturnsHistoryWhenEnabled() {
        assistantProperties.setEnabled(true);
        when(conversationService.getConversationResponse(authenticatedUser))
                .thenReturn(new ConversationResponse(UUID.randomUUID(), List.of()));

        ConversationResponse response = service.getConversation(authenticatedUser);

        assertThat(response.messages()).isEmpty();
    }
}
