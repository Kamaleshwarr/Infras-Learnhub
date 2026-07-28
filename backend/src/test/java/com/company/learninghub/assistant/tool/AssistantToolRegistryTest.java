package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.ResolvedIntent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssistantToolRegistryTest {

    @Test
    void findsMatchingTool() {
        AssistantTool profileTool = mock(AssistantTool.class);
        when(profileTool.supports(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(profileTool.getName()).thenReturn(AssistantToolNames.MY_PROFILE);

        AssistantToolRegistry registry = new AssistantToolRegistry(List.of(profileTool));
        ResolvedIntent intent = ResolvedIntent.tool(AssistantToolNames.MY_PROFILE, "my profile");

        assertThat(registry.findTool(intent)).contains(profileTool);
    }

    @Test
    void executesMatchingTool() {
        AssistantTool profileTool = mock(AssistantTool.class);
        ToolResult expected = ToolResult.text("ok");
        AssistantToolContext context = new AssistantToolContext(null, "my profile");
        ResolvedIntent intent = ResolvedIntent.tool(AssistantToolNames.MY_PROFILE, "my profile");

        when(profileTool.supports(intent)).thenReturn(true);
        when(profileTool.execute(context)).thenReturn(expected);

        AssistantToolRegistry registry = new AssistantToolRegistry(List.of(profileTool));

        assertThat(registry.execute(intent, context)).isEqualTo(expected);
        verify(profileTool).execute(context);
    }

    @Test
    void throwsWhenNoToolMatches() {
        AssistantToolRegistry registry = new AssistantToolRegistry(List.of());
        ResolvedIntent intent = ResolvedIntent.tool(AssistantToolNames.MY_PROFILE, "my profile");

        assertThatThrownBy(() -> registry.execute(intent, new AssistantToolContext(null, "my profile")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("my-profile");
    }

    @Test
    void ignoresToolsThatDoNotSupportIntent() {
        AssistantTool unsupportedTool = mock(AssistantTool.class);
        when(unsupportedTool.supports(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        AssistantToolRegistry registry = new AssistantToolRegistry(List.of(unsupportedTool));
        ResolvedIntent intent = ResolvedIntent.tool(AssistantToolNames.MY_PROFILE, "my profile");

        assertThat(registry.findTool(intent)).isEmpty();
    }
}
