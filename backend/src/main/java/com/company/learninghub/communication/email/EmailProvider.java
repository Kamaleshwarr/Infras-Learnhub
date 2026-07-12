package com.company.learninghub.communication.email;

public interface EmailProvider {

    EmailDeliveryResult send(EmailMessage message);

    boolean isHealthy();

    String providerName();
}
