package com.company.learninghub.notification.service;

import com.company.learninghub.notification.domain.Notification;
import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Component
public class NotificationFactory {

    public Notification certificateSubmitted(User admin, CertificateSubmission submission, Instant createdAt) {
        String employeeName = submission.getEmployee().getFullName();
        String initiativeTitle = submission.getInitiative().getTitle();
        return new Notification(
                admin,
                NotificationType.CERTIFICATE_SUBMITTED,
                "New certificate submission",
                employeeName + " submitted a certificate for \"" + initiativeTitle + "\".",
                NotificationEntityType.CERTIFICATE_SUBMISSION,
                submission.getId(),
                "/submissions/review",
                createdAt
        );
    }

    public Notification certificateApproved(User employee, CertificateSubmission submission, Instant createdAt) {
        String initiativeTitle = submission.getInitiative().getTitle();
        return new Notification(
                employee,
                NotificationType.CERTIFICATE_APPROVED,
                "Certificate approved",
                "Your certificate submission for \"" + initiativeTitle + "\" was approved.",
                NotificationEntityType.CERTIFICATE_SUBMISSION,
                submission.getId(),
                "/submissions",
                createdAt
        );
    }

    public Notification certificateRejected(User employee, CertificateSubmission submission, Instant createdAt) {
        String initiativeTitle = submission.getInitiative().getTitle();
        String reason = submission.getRejectionReason();
        String message = "Your certificate submission for \"" + initiativeTitle + "\" was rejected.";
        if (StringUtils.hasText(reason)) {
            message += " Reason: " + reason.trim();
        }
        return new Notification(
                employee,
                NotificationType.CERTIFICATE_REJECTED,
                "Certificate rejected",
                message,
                NotificationEntityType.CERTIFICATE_SUBMISSION,
                submission.getId(),
                "/submissions",
                createdAt
        );
    }

    public Notification passwordResetByAdmin(User user, Instant createdAt) {
        return new Notification(
                user,
                NotificationType.PASSWORD_RESET_BY_ADMIN,
                "Password reset by administrator",
                "An administrator reset your password. Sign in with your temporary password and change it before continuing.",
                NotificationEntityType.USER,
                user.getId(),
                "/change-password",
                createdAt
        );
    }

    public Notification accountActivated(User user, Instant createdAt) {
        return new Notification(
                user,
                NotificationType.ACCOUNT_ACTIVATED,
                "Account activated",
                "Your Learning Hub account has been activated. You can sign in and access the platform.",
                NotificationEntityType.USER,
                user.getId(),
                "/",
                createdAt
        );
    }

    public Notification accountDeactivated(User user, Instant createdAt) {
        return new Notification(
                user,
                NotificationType.ACCOUNT_DEACTIVATED,
                "Account deactivated",
                "Your Learning Hub account has been deactivated. Contact an administrator if you believe this is an error.",
                NotificationEntityType.USER,
                user.getId(),
                null,
                createdAt
        );
    }

    public Notification accountCreated(User user, Instant createdAt) {
        return new Notification(
                user,
                NotificationType.ACCOUNT_CREATED,
                "Welcome to Learning Hub",
                "Your account has been created. Sign in with the credentials provided by your administrator and change your password on first login.",
                NotificationEntityType.USER,
                user.getId(),
                "/change-password",
                createdAt
        );
    }
}
