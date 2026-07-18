package com.company.learninghub.communication.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogEmailProviderTest {

    private final LogEmailProvider provider = new LogEmailProvider();

    @Test
    void sendReturnsSuccessInLogMode() {
        EmailDeliveryResult result = provider.send(new EmailMessage(
                "noreply@learninghub.local",
                "employee@learninghub.local",
                "Test",
                "Body",
                "<p>Body</p>"
        ));

        assertThat(result.success()).isTrue();
        assertThat(provider.isHealthy()).isTrue();
        assertThat(provider.providerName()).isEqualTo("log");
    }
}
