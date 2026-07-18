package com.company.learninghub.communication.channel;

import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.notification.domain.Notification;
import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InAppChannelHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    private InAppChannelHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InAppChannelHandler(notificationRepository);
    }

    @Test
    void deliverPersistsMappedNotification() {
        User recipient = new User("E001", "employee@learninghub.local", "Jane Doe", "hash");
        UUID submissionId = UUID.randomUUID();
        CommunicationEvent event = new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.CERTIFICATE_APPROVED,
                Instant.parse("2026-07-12T12:00:00Z"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new CommunicationEntityRef("CERTIFICATE_SUBMISSION", submissionId, "/submissions"),
                Map.of(
                        "title", "Certificate approved",
                        "message", "Your certificate submission for \"Cloud\" was approved."
                ),
                Set.of(),
                CommunicationPriority.NORMAL,
                "key"
        );

        handler.deliver(event, recipient);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.CERTIFICATE_APPROVED);
        assertThat(saved.getEntityType()).isEqualTo(NotificationEntityType.CERTIFICATE_SUBMISSION);
        assertThat(saved.getEntityId()).isEqualTo(submissionId);
        assertThat(saved.getActionPath()).isEqualTo("/submissions");
    }
}
