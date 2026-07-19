package com.company.learninghub.communication.email;

import com.company.learninghub.communication.config.CommunicationProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailProviderConfigurationTest {

    private final EmailProviderConfiguration configuration = new EmailProviderConfiguration();
    private final LogEmailProvider logEmailProvider = new LogEmailProvider();
    private final ResendEmailProvider resendEmailProvider = new ResendEmailProvider(
            new CommunicationProperties(),
            new com.fasterxml.jackson.databind.ObjectMapper()
    );

    @Test
    void selectsLogProviderByDefault() {
        CommunicationProperties properties = new CommunicationProperties();

        EmailProvider provider = configuration.emailProvider(
                properties,
                logEmailProvider,
                resendEmailProvider,
                null
        );

        assertThat(provider).isSameAs(logEmailProvider);
        assertThat(provider.providerName()).isEqualTo("log");
    }

    @Test
    void selectsResendProviderWhenConfigured() {
        CommunicationProperties properties = new CommunicationProperties();
        properties.getEmail().setProvider("RESEND");

        EmailProvider provider = configuration.emailProvider(
                properties,
                logEmailProvider,
                resendEmailProvider,
                null
        );

        assertThat(provider).isSameAs(resendEmailProvider);
        assertThat(provider.providerName()).isEqualTo("resend");
    }

    @Test
    void selectsSmtpProviderWhenConfigured() {
        CommunicationProperties properties = new CommunicationProperties();
        properties.getEmail().setProvider("smtp");
        SmtpEmailProvider smtpEmailProvider = new SmtpEmailProvider(properties, null);

        EmailProvider provider = configuration.emailProvider(
                properties,
                logEmailProvider,
                resendEmailProvider,
                smtpEmailProvider
        );

        assertThat(provider).isSameAs(smtpEmailProvider);
        assertThat(provider.providerName()).isEqualTo("smtp");
    }
}
