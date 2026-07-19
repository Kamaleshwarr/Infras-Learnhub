package com.company.learninghub.communication.template;

import com.company.learninghub.communication.domain.CommunicationPriority;

import java.util.Map;

/**
 * Structured model passed to Thymeleaf templates.
 * Supports future attachments, inline images, and localization via {@code extraVariables}.
 */
public record EmailTemplateModel(
        String templateName,
        String subject,
        String recipientName,
        String recipientEmail,
        String actorName,
        String message,
        String actionUrl,
        String actionLabel,
        String applicationUrl,
        String supportEmail,
        String currentYear,
        String projectName,
        String certificationName,
        String technologyName,
        String resetUrl,
        String expirationMinutes,
        CommunicationPriority priority,
        Map<String, String> extraVariables
) {
    public EmailTemplateModel {
        extraVariables = extraVariables == null ? Map.of() : Map.copyOf(extraVariables);
    }
}
