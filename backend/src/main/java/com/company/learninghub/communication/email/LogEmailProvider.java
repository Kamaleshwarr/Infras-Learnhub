package com.company.learninghub.communication.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogEmailProvider implements EmailProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEmailProvider.class);

    @Override
    public EmailDeliveryResult send(EmailMessage message) {
        LOGGER.info(
                "Communication email (log mode). to={}, subject={}, textBody={}",
                message.to(),
                message.subject(),
                message.textBody()
        );
        return EmailDeliveryResult.success("log-mode");
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public String providerName() {
        return "log";
    }
}
