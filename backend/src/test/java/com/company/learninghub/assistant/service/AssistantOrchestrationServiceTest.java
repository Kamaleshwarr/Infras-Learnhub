package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.dto.AssistantOrchestrationResponse;
import com.company.learninghub.assistant.dto.AssistantOutcomeType;
import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.IntentResolver;
import com.company.learninghub.assistant.intent.NavigationTarget;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.assistant.llm.LlmClient;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantOrchestrationServiceTest {

    @Mock
    private LlmClient llmClient;

    @Mock
    private IntentResolver intentResolver;

    @Mock
    private AssistantToolRegistry toolRegistry;

    private AssistantProperties assistantProperties;
    private AssistantOrchestrationService service;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        assistantProperties = new AssistantProperties();
        service = new AssistantOrchestrationService(
                assistantProperties,
                llmClient,
                intentResolver,
                toolRegistry
        );
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        authenticatedUser = AuthenticatedUser.from(user);
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
                new AssistantRequest("my profile"),
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
                new AssistantRequest("open projects"),
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

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("my profile"),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.TOOL);
        assertThat(response.toolResult()).isEqualTo(toolResult);
    }

    @Test
    void processRequestReturnsKnowledgePlaceholder() {
        assistantProperties.setEnabled(true);
        when(intentResolver.resolve("what is docker")).thenReturn(ResolvedIntent.knowledge("what is docker"));

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("what is docker"),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.KNOWLEDGE);
        assertThat(response.message()).contains("not available yet");
    }

    @Test
    void processRequestReturnsUnknownOutcome() {
        assistantProperties.setEnabled(true);
        when(intentResolver.resolve("???")).thenReturn(ResolvedIntent.unknown(""));

        AssistantOrchestrationResponse response = service.processRequest(
                new AssistantRequest("???"),
                authenticatedUser
        );

        assertThat(response.outcomeType()).isEqualTo(AssistantOutcomeType.UNKNOWN);
    }
}
