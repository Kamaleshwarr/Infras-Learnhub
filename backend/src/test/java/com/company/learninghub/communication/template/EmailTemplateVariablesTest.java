package com.company.learninghub.communication.template;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateVariablesTest {

    private EmailTemplateVariables templateVariables;
    private CommunicationProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CommunicationProperties();
        properties.setFrontendBaseUrl("http://localhost:5173");
        properties.setSupportEmail("support@learninghub.local");
        templateVariables = new EmailTemplateVariables(properties);
    }

    @Test
    void buildsModelFromEventWithDefaults() {
        User recipient = new User("E001", "jane@example.com", "Jane Doe", "hash");
        CommunicationEvent event = event(
                CommunicationEventType.CERTIFICATE_APPROVED,
                Map.of("message", "Approved for AWS track."),
                "/certifications"
        );

        EmailTemplateModel model = templateVariables.build(recipient, event);

        assertThat(model.templateName()).isEqualTo("certificate-approved");
        assertThat(model.subject()).isEqualTo("Certificate approved");
        assertThat(model.recipientName()).isEqualTo("Jane Doe");
        assertThat(model.recipientEmail()).isEqualTo("jane@example.com");
        assertThat(model.message()).isEqualTo("Approved for AWS track.");
        assertThat(model.actionUrl()).isEqualTo("http://localhost:5173/certifications");
        assertThat(model.actionLabel()).isEqualTo("View certifications");
        assertThat(model.supportEmail()).isEqualTo("support@learninghub.local");
        assertThat(model.currentYear()).isNotBlank();
    }

    @Test
    void prefersExplicitEmailSubjectAndActionUrl() {
        User recipient = new User("E001", "jane@example.com", "Jane Doe", "hash");
        CommunicationEvent event = event(
                CommunicationEventType.PROJECT_MEMBER_ADDED,
                Map.of(
                        "emailSubject", "Custom subject",
                        "actionUrl", "https://example.com/projects/1",
                        "actionLabel", "Open project",
                        "projectName", "Core Platform"
                ),
                "/projects/1"
        );

        EmailTemplateModel model = templateVariables.build(recipient, event);

        assertThat(model.subject()).isEqualTo("Custom subject");
        assertThat(model.actionUrl()).isEqualTo("https://example.com/projects/1");
        assertThat(model.actionLabel()).isEqualTo("Open project");
        assertThat(model.projectName()).isEqualTo("Core Platform");
        assertThat(model.extraVariables()).doesNotContainKeys("emailSubject", "actionUrl", "actionLabel");
    }

    @Test
    void mapsInitiativeTitleToCertificationName() {
        User recipient = new User("E001", "jane@example.com", "Jane Doe", "hash");
        CommunicationEvent event = event(
                CommunicationEventType.CERTIFICATE_SUBMITTED,
                Map.of("initiativeTitle", "Kubernetes Admin"),
                "/submissions/review"
        );

        EmailTemplateModel model = templateVariables.build(recipient, event);

        assertThat(model.certificationName()).isEqualTo("Kubernetes Admin");
    }

    @Test
    void preservesUnknownVariablesInExtras() {
        User recipient = new User("E001", "jane@example.com", "Jane Doe", "hash");
        CommunicationEvent event = event(
                CommunicationEventType.CERTIFICATE_REJECTED,
                Map.of("rejectionReason", "Incomplete evidence"),
                "/submissions"
        );

        EmailTemplateModel model = templateVariables.build(recipient, event);

        assertThat(model.extraVariables()).containsEntry("rejectionReason", "Incomplete evidence");
    }

    private CommunicationEvent event(
            CommunicationEventType type,
            Map<String, String> variables,
            String actionPath
    ) {
        return new CommunicationEvent(
                UUID.randomUUID(),
                type,
                Instant.parse("2026-07-12T12:00:00Z"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new CommunicationEntityRef("ENTITY", UUID.randomUUID(), actionPath),
                variables,
                Set.of(),
                CommunicationPriority.NORMAL,
                "variables-test"
        );
    }
}
