package com.company.learninghub.communication.email;

import com.company.learninghub.communication.config.CommunicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmailProviderConfiguration {

    @Bean
    @Primary
    public EmailProvider emailProvider(
            CommunicationProperties communicationProperties,
            LogEmailProvider logEmailProvider,
            @Autowired(required = false) SmtpEmailProvider smtpEmailProvider
    ) {
        if (communicationProperties.getEmail().isLogMode()) {
            return logEmailProvider;
        }
        if (smtpEmailProvider != null) {
            return smtpEmailProvider;
        }
        return logEmailProvider;
    }
}
