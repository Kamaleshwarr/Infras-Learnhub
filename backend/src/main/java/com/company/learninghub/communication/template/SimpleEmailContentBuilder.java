package com.company.learninghub.communication.template;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.email.EmailMessage;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SimpleEmailContentBuilder {

    private final CommunicationProperties communicationProperties;

    public SimpleEmailContentBuilder(CommunicationProperties communicationProperties) {
        this.communicationProperties = communicationProperties;
    }

    public EmailMessage build(User recipient, CommunicationEvent event) {
        String from = communicationProperties.getEmail().getFrom();
        String subject = resolveSubject(event);
        String textBody = resolveTextBody(recipient, event);
        String htmlBody = "<html><body><pre style=\"font-family: sans-serif;\">"
                + escapeHtml(textBody)
                + "</pre></body></html>";
        return new EmailMessage(from, recipient.getEmail(), subject, textBody, htmlBody);
    }

    private String resolveSubject(CommunicationEvent event) {
        String custom = event.variables().get("emailSubject");
        if (StringUtils.hasText(custom)) {
            return custom.trim();
        }
        return switch (event.type()) {
            case PASSWORD_RESET_REQUESTED -> "Reset your Learning Hub password";
            case PASSWORD_RESET_BY_ADMIN -> "Your password was reset";
            case ACCOUNT_CREATED -> "Welcome to Engineering Learning Hub";
            case ACCOUNT_ACTIVATED -> "Your account has been activated";
            case ACCOUNT_DEACTIVATED -> "Your account has been deactivated";
            case CERTIFICATE_SUBMITTED -> "New certificate submission";
            case CERTIFICATE_APPROVED -> "Certificate approved";
            case CERTIFICATE_REJECTED -> "Certificate requires attention";
            case LEARNING_STAGE_COMPLETED -> "Learning stage completed";
            case LEARNING_ROADMAP_COMPLETED -> "Learning roadmap completed";
            case CONTINUE_LEARNING_REMINDER -> "Continue your learning journey";
            case PROJECT_MEMBER_ADDED -> "You were added to a project";
            case PROJECT_REPOSITORY_ADDED -> "Repository added to project";
            case PROJECT_ENVIRONMENT_ADDED -> "Environment added to project";
        };
    }

    private String resolveTextBody(User recipient, CommunicationEvent event) {
        String custom = event.variables().get("emailBody");
        if (StringUtils.hasText(custom)) {
            return custom.trim();
        }
        String greeting = "Hello " + recipient.getFullName() + ",";
        String message = event.variables().getOrDefault("message", defaultMessage(event));
        String actionPath = event.entityRef() == null ? null : event.entityRef().actionPath();
        StringBuilder body = new StringBuilder(greeting).append("\n\n").append(message);
        if (StringUtils.hasText(actionPath)) {
            body.append("\n\nOpen in Learning Hub: ")
                    .append(communicationProperties.getFrontendBaseUrl())
                    .append(actionPath);
        }
        body.append("\n\n— Engineering Learning Hub");
        return body.toString();
    }

    private String defaultMessage(CommunicationEvent event) {
        return switch (event.type()) {
            case CERTIFICATE_APPROVED -> "Your certificate submission was approved.";
            case CERTIFICATE_REJECTED -> "Your certificate submission requires attention.";
            case CERTIFICATE_SUBMITTED -> "A new certificate submission is ready for review.";
            case ACCOUNT_CREATED -> "Your account has been created.";
            case ACCOUNT_ACTIVATED -> "Your account has been activated.";
            case ACCOUNT_DEACTIVATED -> "Your account has been deactivated.";
            default -> "You have a new notification from Engineering Learning Hub.";
        };
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
