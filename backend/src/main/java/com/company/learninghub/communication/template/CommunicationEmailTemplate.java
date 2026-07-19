package com.company.learninghub.communication.template;

import com.company.learninghub.communication.domain.CommunicationEventType;

public enum CommunicationEmailTemplate {

    CERTIFICATE_SUBMITTED("certificate-submitted", "New certificate submission"),
    CERTIFICATE_APPROVED("certificate-approved", "Certificate approved"),
    CERTIFICATE_REJECTED("certificate-rejected", "Certificate requires attention"),
    ACCOUNT_CREATED("account-created", "Welcome to Engineering Learning Hub"),
    ACCOUNT_ACTIVATED("account-activated", "Your account has been activated"),
    ACCOUNT_DEACTIVATED("account-deactivated", "Your account has been deactivated"),
    PASSWORD_RESET("password-reset", "Reset your Learning Hub password"),
    PASSWORD_RESET_BY_ADMIN("password-reset-by-admin", "Your password was reset"),
    PROJECT_MEMBER_ADDED("project-member-added", "You were added to a project"),
    PROJECT_REPOSITORY_ADDED("project-repository-added", "Repository added to project"),
    PROJECT_ENVIRONMENT_ADDED("project-environment-added", "Environment added to project"),
    GENERIC_NOTIFICATION("generic-notification", "Notification from Engineering Learning Hub");

    private final String templateName;
    private final String defaultSubject;

    CommunicationEmailTemplate(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    public String templateName() {
        return templateName;
    }

    public String defaultSubject() {
        return defaultSubject;
    }

    public static CommunicationEmailTemplate fromEventType(CommunicationEventType eventType) {
        return switch (eventType) {
            case CERTIFICATE_SUBMITTED -> CERTIFICATE_SUBMITTED;
            case CERTIFICATE_APPROVED -> CERTIFICATE_APPROVED;
            case CERTIFICATE_REJECTED -> CERTIFICATE_REJECTED;
            case ACCOUNT_CREATED -> ACCOUNT_CREATED;
            case ACCOUNT_ACTIVATED -> ACCOUNT_ACTIVATED;
            case ACCOUNT_DEACTIVATED -> ACCOUNT_DEACTIVATED;
            case PASSWORD_RESET_REQUESTED -> PASSWORD_RESET;
            case PASSWORD_RESET_BY_ADMIN -> PASSWORD_RESET_BY_ADMIN;
            case PROJECT_MEMBER_ADDED -> PROJECT_MEMBER_ADDED;
            case PROJECT_REPOSITORY_ADDED -> PROJECT_REPOSITORY_ADDED;
            case PROJECT_ENVIRONMENT_ADDED -> PROJECT_ENVIRONMENT_ADDED;
            case LEARNING_STAGE_COMPLETED,
                 LEARNING_ROADMAP_COMPLETED,
                 CONTINUE_LEARNING_REMINDER -> GENERIC_NOTIFICATION;
        };
    }
}
