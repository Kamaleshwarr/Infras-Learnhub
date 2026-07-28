package com.company.learninghub.assistant.intent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NavigationIntentResolverTest {

    private NavigationIntentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new NavigationIntentResolver();
    }

    @Test
    void resolvesProjectsNavigation() {
        Optional<NavigationTarget> target = resolver.resolve("Open Projects");

        assertThat(target).isPresent();
        assertThat(target.get().path()).isEqualTo("/projects");
        assertThat(target.get().label()).isEqualTo("Projects");
    }

    @Test
    void resolvesLearnNavigation() {
        Optional<NavigationTarget> target = resolver.resolve("Go to Learn");

        assertThat(target).isPresent();
        assertThat(target.get().path()).isEqualTo("/learn");
        assertThat(target.get().label()).isEqualTo("Learn");
    }

    @Test
    void resolvesLeaderboardsNavigation() {
        Optional<NavigationTarget> target = resolver.resolve("Open Leaderboards");

        assertThat(target).isPresent();
        assertThat(target.get().path()).isEqualTo("/leaderboards/global");
        assertThat(target.get().label()).isEqualTo("Leaderboards");
    }

    @Test
    void resolvesDashboardNavigation() {
        Optional<NavigationTarget> target = resolver.resolve("Open Dashboard");

        assertThat(target).isPresent();
        assertThat(target.get().path()).isEqualTo("/");
        assertThat(target.get().label()).isEqualTo("Dashboard");
    }

    @Test
    void returnsEmptyForUnrecognizedNavigation() {
        Optional<NavigationTarget> target = resolver.resolve("show my profile");

        assertThat(target).isEmpty();
    }
}
