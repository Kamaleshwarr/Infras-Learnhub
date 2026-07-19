package com.company.learninghub.communication.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommunicationPropertiesResendBindingTest {

    @Test
    void bindsResendProviderConfigurationFromEnvironmentStyleProperties() {
        Map<String, String> properties = Map.of(
                "app.communication.email.provider", "resend",
                "app.communication.email.resend.api-key", "re_from_env",
                "app.communication.email.resend.base-url", "https://api.resend.com",
                "app.communication.email.resend.connect-timeout", "PT5S",
                "app.communication.email.resend.read-timeout", "PT15S"
        );

        CommunicationProperties bound = new Binder(new MapConfigurationPropertySource(properties))
                .bind("app.communication", CommunicationProperties.class)
                .orElseThrow(() -> new AssertionError("Failed to bind CommunicationProperties"));

        assertThat(bound.getEmail().isResendMode()).isTrue();
        assertThat(bound.getEmail().getResend().getApiKey()).isEqualTo("re_from_env");
        assertThat(bound.getEmail().getResend().getBaseUrl()).isEqualTo("https://api.resend.com");
        assertThat(bound.getEmail().getResend().getConnectTimeout().getSeconds()).isEqualTo(5);
        assertThat(bound.getEmail().getResend().getReadTimeout().getSeconds()).isEqualTo(15);
    }
}
