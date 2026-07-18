package com.company.learninghub.communication.email;

public record EmailDeliveryResult(
        boolean success,
        String providerMessageId,
        String errorMessage
) {
    public static EmailDeliveryResult success(String providerMessageId) {
        return new EmailDeliveryResult(true, providerMessageId, null);
    }

    public static EmailDeliveryResult failure(String errorMessage) {
        return new EmailDeliveryResult(false, null, errorMessage);
    }
}
