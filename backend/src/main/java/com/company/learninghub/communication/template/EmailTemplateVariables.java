package com.company.learninghub.communication.template;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailTemplateVariables {

    private final CommunicationProperties communicationProperties;

    public EmailTemplateVariables(CommunicationProperties communicationProperties) {
        this.communicationProperties = communicationProperties;
    }

    public EmailTemplateModel build(User recipient, CommunicationEvent event) {
        CommunicationEmailTemplate template = CommunicationEmailTemplate.fromEventType(event.type());
        Map<String, String> variables = event.variables();

        String subject = firstNonBlank(
                variables.get("emailSubject"),
                template.defaultSubject()
        );
        String message = firstNonBlank(
                variables.get("message"),
                variables.get("emailBody"),
                defaultMessage(template)
        );
        String actionPath = event.entityRef() == null ? null : event.entityRef().actionPath();
        String actionUrl = firstNonBlank(
                variables.get("actionUrl"),
                variables.get("resetUrl"),
                buildActionUrl(actionPath)
        );
        String actionLabel = firstNonBlank(
                variables.get("actionLabel"),
                defaultActionLabel(template)
        );

        Map<String, String> extras = new HashMap<>(variables);
        extras.remove("emailSubject");
        extras.remove("emailBody");
        extras.remove("message");
        extras.remove("actionUrl");
        extras.remove("actionLabel");

        return new EmailTemplateModel(
                template.templateName(),
                subject,
                recipient.getFullName(),
                recipient.getEmail(),
                variables.get("actorName"),
                message,
                actionUrl,
                actionLabel,
                communicationProperties.getFrontendBaseUrl(),
                communicationProperties.getSupportEmail(),
                String.valueOf(Year.now().getValue()),
                variables.get("projectName"),
                firstNonBlank(variables.get("certificationName"), variables.get("initiativeTitle")),
                variables.get("technologyName"),
                variables.get("resetUrl"),
                variables.get("expirationMinutes"),
                event.priority(),
                Map.copyOf(extras)
        );
    }

    private String buildActionUrl(String actionPath) {
        if (!StringUtils.hasText(actionPath)) {
            return null;
        }
        String baseUrl = communicationProperties.getFrontendBaseUrl();
        if (actionPath.startsWith("http://") || actionPath.startsWith("https://")) {
            return actionPath;
        }
        if (baseUrl.endsWith("/") && actionPath.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + actionPath;
        }
        if (!baseUrl.endsWith("/") && !actionPath.startsWith("/")) {
            return baseUrl + "/" + actionPath;
        }
        return baseUrl + actionPath;
    }

    private String defaultMessage(CommunicationEmailTemplate template) {
        return switch (template) {
            case CERTIFICATE_APPROVED -> "Your certificate submission was approved.";
            case CERTIFICATE_REJECTED -> "Your certificate submission requires attention.";
            case CERTIFICATE_SUBMITTED -> "A new certificate submission is ready for review.";
            case ACCOUNT_CREATED -> "Your account has been created. Sign in with the credentials provided by your administrator.";
            case ACCOUNT_ACTIVATED -> "Your Learning Hub account has been activated.";
            case ACCOUNT_DEACTIVATED -> "Your Learning Hub account has been deactivated.";
            case PASSWORD_RESET -> "We received a request to reset your password.";
            case PASSWORD_RESET_BY_ADMIN -> "An administrator reset your password. Sign in with your temporary password and change it before continuing.";
            case PROJECT_MEMBER_ADDED -> "You have been added to a project team.";
            case PROJECT_REPOSITORY_ADDED -> "A repository link was added to your project.";
            case PROJECT_ENVIRONMENT_ADDED -> "An environment was added to your project.";
            case GENERIC_NOTIFICATION -> "You have a new notification from Engineering Learning Hub.";
        };
    }

    private String defaultActionLabel(CommunicationEmailTemplate template) {
        return switch (template) {
            case CERTIFICATE_SUBMITTED -> "Review submission";
            case CERTIFICATE_APPROVED, CERTIFICATE_REJECTED -> "View certifications";
            case ACCOUNT_CREATED, ACCOUNT_ACTIVATED, PASSWORD_RESET_BY_ADMIN -> "Sign in";
            case PASSWORD_RESET -> "Reset password";
            case PROJECT_MEMBER_ADDED, PROJECT_REPOSITORY_ADDED, PROJECT_ENVIRONMENT_ADDED -> "View project";
            case ACCOUNT_DEACTIVATED -> null;
            case GENERIC_NOTIFICATION -> "Open Learning Hub";
        };
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
