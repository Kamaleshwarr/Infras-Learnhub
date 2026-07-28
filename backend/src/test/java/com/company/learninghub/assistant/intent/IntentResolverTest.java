package com.company.learninghub.assistant.intent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntentResolverTest {

    @Mock
    private NavigationIntentResolver navigationIntentResolver;

    private IntentResolver intentResolver;

    @BeforeEach
    void setUp() {
        intentResolver = new IntentResolver(navigationIntentResolver);
    }

    @Test
    void resolvesNavigationBeforeTools() {
        when(navigationIntentResolver.resolve("open projects"))
                .thenReturn(Optional.of(new NavigationTarget("/projects", "Projects")));

        ResolvedIntent intent = intentResolver.resolve("open projects");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.NAVIGATION);
        assertThat(intent.navigationTarget().path()).isEqualTo("/projects");
    }

    @Test
    void resolvesProfileToolIntent() {
        when(navigationIntentResolver.resolve("show my profile")).thenReturn(Optional.empty());

        ResolvedIntent intent = intentResolver.resolve("show my profile");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.TOOL);
        assertThat(intent.toolName()).isEqualTo("my-profile");
    }

    @Test
    void resolvesLeaderboardToolIntent() {
        when(navigationIntentResolver.resolve("my rank")).thenReturn(Optional.empty());

        ResolvedIntent intent = intentResolver.resolve("my rank");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.TOOL);
        assertThat(intent.toolName()).isEqualTo("my-leaderboard-rank");
    }

    @Test
    void resolvesCertificationsToolIntent() {
        when(navigationIntentResolver.resolve("my certifications")).thenReturn(Optional.empty());

        ResolvedIntent intent = intentResolver.resolve("my certifications");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.TOOL);
        assertThat(intent.toolName()).isEqualTo("my-certifications");
    }

    @Test
    void resolvesLearningInitiativesToolIntent() {
        when(navigationIntentResolver.resolve("available learning initiatives")).thenReturn(Optional.empty());

        ResolvedIntent intent = intentResolver.resolve("available learning initiatives");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.TOOL);
        assertThat(intent.toolName()).isEqualTo("available-learning-initiatives");
    }

    @Test
    void resolvesKnowledgeIntent() {
        when(navigationIntentResolver.resolve("what is spring boot")).thenReturn(Optional.empty());

        ResolvedIntent intent = intentResolver.resolve("what is spring boot");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.KNOWLEDGE);
    }

    @Test
    void resolvesUnknownIntent() {
        when(navigationIntentResolver.resolve("random gibberish")).thenReturn(Optional.empty());

        ResolvedIntent intent = intentResolver.resolve("random gibberish");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.UNKNOWN);
    }

    @Test
    void resolvesUnknownForBlankMessage() {
        ResolvedIntent intent = intentResolver.resolve("   ");

        assertThat(intent.type()).isEqualTo(AssistantIntentType.UNKNOWN);
    }
}
