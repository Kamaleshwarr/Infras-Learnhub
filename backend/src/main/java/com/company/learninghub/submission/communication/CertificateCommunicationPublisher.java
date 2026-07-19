package com.company.learninghub.submission.communication;

import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.service.CommunicationService;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Publishes certificate workflow communication events through the Communication Framework.
 * Channel selection and template mapping remain encapsulated here so domain services stay channel-agnostic.
 */
@Component
public class CertificateCommunicationPublisher {

    private static final Set<CommunicationChannel> IN_APP_AND_EMAIL = Set.of(
            CommunicationChannel.IN_APP,
            CommunicationChannel.EMAIL
    );

    private final CommunicationService communicationService;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public CertificateCommunicationPublisher(
            CommunicationService communicationService,
            UserRepository userRepository
    ) {
        this(communicationService, userRepository, Clock.systemUTC());
    }

    CertificateCommunicationPublisher(
            CommunicationService communicationService,
            UserRepository userRepository,
            Clock clock
    ) {
        this.communicationService = communicationService;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public void publishSubmitted(CertificateSubmission submission) {
        Instant occurredAt = Instant.now(clock);
        User employee = submission.getEmployee();
        String initiativeTitle = submission.getInitiative().getTitle();
        String message = employee.getFullName() + " submitted a certificate for \"" + initiativeTitle + "\".";

        List<User> admins = userRepository.findActiveByRoleName(RoleName.ADMIN);
        for (User admin : admins) {
            CommunicationEvent event = new CommunicationEvent(
                    UUID.randomUUID(),
                    CommunicationEventType.CERTIFICATE_SUBMITTED,
                    occurredAt,
                    employee.getId(),
                    admin.getId(),
                    new CommunicationEntityRef(
                            "CERTIFICATE_SUBMISSION",
                            submission.getId(),
                            "/submissions/review"
                    ),
                    Map.of(
                            "title", "New certificate submission",
                            "message", message,
                            "actorName", employee.getFullName(),
                            "initiativeTitle", initiativeTitle,
                            "certificationName", initiativeTitle
                    ),
                    IN_APP_AND_EMAIL,
                    CommunicationPriority.HIGH,
                    idempotencyKey(CommunicationEventType.CERTIFICATE_SUBMITTED, submission.getId(), admin.getId())
            );
            communicationService.publish(event);
        }
    }

    public void publishApproved(CertificateSubmission submission, User reviewer) {
        publishEmployeeOutcome(
                submission,
                reviewer,
                CommunicationEventType.CERTIFICATE_APPROVED,
                "Certificate approved",
                "Your certificate submission for \"" + submission.getInitiative().getTitle() + "\" was approved.",
                CommunicationPriority.NORMAL
        );
    }

    public void publishRejected(CertificateSubmission submission, User reviewer) {
        String initiativeTitle = submission.getInitiative().getTitle();
        String message = "Your certificate submission for \"" + initiativeTitle + "\" was rejected.";
        if (StringUtils.hasText(submission.getRejectionReason())) {
            message += " Reason: " + submission.getRejectionReason().trim();
        }

        publishEmployeeOutcome(
                submission,
                reviewer,
                CommunicationEventType.CERTIFICATE_REJECTED,
                "Certificate rejected",
                message,
                CommunicationPriority.NORMAL,
                Map.of("rejectionReason", submission.getRejectionReason().trim())
        );
    }

    private void publishEmployeeOutcome(
            CertificateSubmission submission,
            User reviewer,
            CommunicationEventType eventType,
            String title,
            String message,
            CommunicationPriority priority
    ) {
        publishEmployeeOutcome(submission, reviewer, eventType, title, message, priority, Map.of());
    }

    private void publishEmployeeOutcome(
            CertificateSubmission submission,
            User reviewer,
            CommunicationEventType eventType,
            String title,
            String message,
            CommunicationPriority priority,
            Map<String, String> extraVariables
    ) {
        String initiativeTitle = submission.getInitiative().getTitle();
        Map<String, String> variables = new java.util.HashMap<>(extraVariables);
        variables.put("title", title);
        variables.put("message", message);
        variables.put("initiativeTitle", initiativeTitle);
        variables.put("certificationName", initiativeTitle);

        CommunicationEvent event = new CommunicationEvent(
                UUID.randomUUID(),
                eventType,
                Instant.now(clock),
                reviewer.getId(),
                submission.getEmployee().getId(),
                new CommunicationEntityRef(
                        "CERTIFICATE_SUBMISSION",
                        submission.getId(),
                        "/submissions"
                ),
                Map.copyOf(variables),
                IN_APP_AND_EMAIL,
                priority,
                idempotencyKey(eventType, submission.getId(), submission.getEmployee().getId())
        );
        communicationService.publish(event);
    }

    private String idempotencyKey(CommunicationEventType eventType, UUID submissionId, UUID recipientUserId) {
        return eventType.name() + ":" + submissionId + ":" + recipientUserId;
    }
}
