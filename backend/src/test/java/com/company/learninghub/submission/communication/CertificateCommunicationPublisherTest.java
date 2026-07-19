package com.company.learninghub.submission.communication;

import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.service.CommunicationService;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.domain.CertificateDocument;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateCommunicationPublisherTest {

    private static final Instant NOW = Instant.parse("2026-07-19T12:00:00Z");

    @Mock
    private CommunicationService communicationService;

    @Mock
    private UserRepository userRepository;

    private CertificateCommunicationPublisher publisher;
    private User employee;
    private User admin;
    private LearningInitiative initiative;
    private CertificateSubmission submission;

    @BeforeEach
    void setUp() {
        publisher = new CertificateCommunicationPublisher(
                communicationService,
                userRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        employee = user("EMP001", "employee@learninghub.local", RoleName.EMPLOYEE);
        admin = user("ADMIN001", "admin@learninghub.local", RoleName.ADMIN);
        initiative = initiative("AWS Certification");
        submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
    }

    @Test
    void publishSubmittedFansOutToActiveAdmins() {
        when(userRepository.findActiveByRoleName(RoleName.ADMIN)).thenReturn(List.of(admin));

        publisher.publishSubmitted(submission);

        ArgumentCaptor<CommunicationEvent> captor = ArgumentCaptor.forClass(CommunicationEvent.class);
        verify(communicationService).publish(captor.capture());
        CommunicationEvent event = captor.getValue();

        assertThat(event.type()).isEqualTo(CommunicationEventType.CERTIFICATE_SUBMITTED);
        assertThat(event.recipientUserId()).isEqualTo(admin.getId());
        assertThat(event.actorUserId()).isEqualTo(employee.getId());
        assertThat(event.channels()).containsExactlyInAnyOrder(
                CommunicationChannel.IN_APP,
                CommunicationChannel.EMAIL
        );
        assertThat(event.priority()).isEqualTo(CommunicationPriority.HIGH);
        assertThat(event.variables().get("title")).isEqualTo("New certificate submission");
        assertThat(event.variables().get("message"))
                .isEqualTo("EMP001 User submitted a certificate for \"AWS Certification\".");
        assertThat(event.variables().get("actorName")).isEqualTo("EMP001 User");
        assertThat(event.variables().get("initiativeTitle")).isEqualTo("AWS Certification");
        assertThat(event.entityRef().actionPath()).isEqualTo("/submissions/review");
        assertThat(event.idempotencyKey())
                .isEqualTo("CERTIFICATE_SUBMITTED:" + submission.getId() + ":" + admin.getId());
    }

    @Test
    void publishApprovedTargetsEmployeeWithExpectedPayload() {
        publisher.publishApproved(submission, admin);

        ArgumentCaptor<CommunicationEvent> captor = ArgumentCaptor.forClass(CommunicationEvent.class);
        verify(communicationService).publish(captor.capture());
        CommunicationEvent event = captor.getValue();

        assertThat(event.type()).isEqualTo(CommunicationEventType.CERTIFICATE_APPROVED);
        assertThat(event.recipientUserId()).isEqualTo(employee.getId());
        assertThat(event.actorUserId()).isEqualTo(admin.getId());
        assertThat(event.channels()).containsExactlyInAnyOrder(
                CommunicationChannel.IN_APP,
                CommunicationChannel.EMAIL
        );
        assertThat(event.priority()).isEqualTo(CommunicationPriority.NORMAL);
        assertThat(event.variables().get("title")).isEqualTo("Certificate approved");
        assertThat(event.variables().get("message"))
                .isEqualTo("Your certificate submission for \"AWS Certification\" was approved.");
        assertThat(event.entityRef().actionPath()).isEqualTo("/submissions");
    }

    @Test
    void publishRejectedIncludesReasonForTemplatesAndInAppCopy() {
        submission.reject(admin, NOW, "Name mismatch");

        publisher.publishRejected(submission, admin);

        ArgumentCaptor<CommunicationEvent> captor = ArgumentCaptor.forClass(CommunicationEvent.class);
        verify(communicationService).publish(captor.capture());
        CommunicationEvent event = captor.getValue();

        assertThat(event.type()).isEqualTo(CommunicationEventType.CERTIFICATE_REJECTED);
        assertThat(event.recipientUserId()).isEqualTo(employee.getId());
        assertThat(event.variables().get("title")).isEqualTo("Certificate rejected");
        assertThat(event.variables().get("message"))
                .isEqualTo("Your certificate submission for \"AWS Certification\" was rejected. Reason: Name mismatch");
        assertThat(event.variables().get("rejectionReason")).isEqualTo("Name mismatch");
    }

    @Test
    void publishSubmittedSkipsWhenNoAdminsExist() {
        when(userRepository.findActiveByRoleName(RoleName.ADMIN)).thenReturn(List.of());

        publisher.publishSubmitted(submission);

        verify(communicationService, times(0)).publish(org.mockito.ArgumentMatchers.any());
    }

    private User user(String employeeId, String email, RoleName roleName) {
        User user = new User(employeeId, email, employeeId + " User", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private LearningInitiative initiative(String title) {
        LearningInitiative initiative = new LearningInitiative(
                title,
                title + " description",
                "Reward",
                NOW.minusSeconds(3600),
                NOW.plusSeconds(3600),
                InitiativeStatus.ACTIVE,
                admin
        );
        ReflectionTestUtils.setField(initiative, "id", UUID.randomUUID());
        return initiative;
    }

    private CertificateSubmission submission(User employee, LearningInitiative initiative, ApprovalStatus status) {
        CertificateDocument document = new CertificateDocument(
                "LOCAL",
                "certificates/file.pdf",
                "certificate.pdf",
                "application/pdf",
                12,
                employee
        );
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());
        CertificateSubmission submission = new CertificateSubmission(
                employee,
                initiative,
                document,
                "comments",
                NOW.minusSeconds(60)
        );
        ReflectionTestUtils.setField(submission, "id", UUID.randomUUID());
        if (ApprovalStatus.REJECTED.equals(status)) {
            submission.reject(admin, NOW, "Rejected");
        }
        return submission;
    }
}
