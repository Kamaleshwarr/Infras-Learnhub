package com.company.learninghub.communication.template;

public record RenderedEmail(
        String templateName,
        String subject,
        String htmlBody,
        String textBody
) {
}
