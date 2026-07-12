package com.company.learninghub.communication.channel;

import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.notification.domain.Notification;
import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InAppChannelHandler {

    private final NotificationRepository notificationRepository;

    public InAppChannelHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void deliver(CommunicationEvent event, User recipient) {
        Notification notification = toNotification(event, recipient);
        notificationRepository.save(notification);
    }

    private Notification toNotification(CommunicationEvent event, User recipient) {
        String title = requiredVariable(event, "title");
        String message = requiredVariable(event, "message");
        NotificationEntityType entityType = mapEntityType(event);
        return new Notification(
                recipient,
                mapNotificationType(event.type()),
                title,
                message,
                entityType,
                event.entityRef() == null ? null : event.entityRef().entityId(),
                event.entityRef() == null ? null : event.entityRef().actionPath(),
                event.occurredAt()
        );
    }

    private String requiredVariable(CommunicationEvent event, String key) {
        String value = event.variables().get(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Missing required in-app variable: " + key);
        }
        return value.trim();
    }

    private NotificationType mapNotificationType(CommunicationEventType eventType) {
        return switch (eventType) {
            case CERTIFICATE_SUBMITTED -> NotificationType.CERTIFICATE_SUBMITTED;
            case CERTIFICATE_APPROVED -> NotificationType.CERTIFICATE_APPROVED;
            case CERTIFICATE_REJECTED -> NotificationType.CERTIFICATE_REJECTED;
            case PASSWORD_RESET_BY_ADMIN -> NotificationType.PASSWORD_RESET_BY_ADMIN;
            case ACCOUNT_ACTIVATED -> NotificationType.ACCOUNT_ACTIVATED;
            case ACCOUNT_DEACTIVATED -> NotificationType.ACCOUNT_DEACTIVATED;
            case ACCOUNT_CREATED -> NotificationType.ACCOUNT_CREATED;
            default -> throw new IllegalArgumentException("Unsupported in-app event type: " + eventType);
        };
    }

    private NotificationEntityType mapEntityType(CommunicationEvent event) {
        if (event.entityRef() == null || !StringUtils.hasText(event.entityRef().entityType())) {
            return null;
        }
        return NotificationEntityType.valueOf(event.entityRef().entityType());
    }
}
