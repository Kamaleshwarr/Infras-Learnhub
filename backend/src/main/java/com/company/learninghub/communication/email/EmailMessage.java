package com.company.learninghub.communication.email;

public record EmailMessage(
        String from,
        String to,
        String subject,
        String textBody,
        String htmlBody
) {
}
