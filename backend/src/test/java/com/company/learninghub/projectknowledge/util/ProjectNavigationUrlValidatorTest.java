package com.company.learninghub.projectknowledge.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectNavigationUrlValidatorTest {

    @Test
    void acceptsHttpsUrl() {
        assertThat(ProjectNavigationUrlValidator.normalizeNavigationUrl("https://example.com/app", "URL"))
                .isEqualTo("https://example.com/app");
    }

    @Test
    void rejectsEmbeddedCredentials() {
        assertThatThrownBy(() -> ProjectNavigationUrlValidator.normalizeNavigationUrl(
                "https://user:password@example.com/path",
                "URL"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL must not contain embedded credentials");
    }

    @Test
    void rejectsMissingScheme() {
        assertThatThrownBy(() -> ProjectNavigationUrlValidator.normalizeNavigationUrl("example.com", "URL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL must start with http:// or https://");
    }
}
