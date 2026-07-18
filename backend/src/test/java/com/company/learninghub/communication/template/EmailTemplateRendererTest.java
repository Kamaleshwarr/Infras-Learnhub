package com.company.learninghub.communication.template;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationEntityRef;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationEventType;
import com.company.learninghub.communication.domain.CommunicationPriority;
import com.company.learninghub.communication.email.EmailMessage;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateRendererTest {

    private EmailTemplateRenderer renderer;
    private CommunicationProperties properties;

    @BeforeEach
    void setUp() {
        CommunicationEmailTemplateConfiguration configuration = new CommunicationEmailTemplateConfiguration();
        SpringTemplateEngine htmlEngine = configuration.communicationHtmlTemplateEngine();
        SpringTemplateEngine textEngine = configuration.communicationTextTemplateEngine();
        properties = new CommunicationProperties();
        properties.setFrontendBaseUrl("http://localhost:5173");
        properties.setSupportEmail("support@learninghub.local");
        properties.getEmail().setFrom("noreply@learninghub.local");
        EmailTemplateVariables templateVariables = new EmailTemplateVariables(properties);
        renderer = new EmailTemplateRenderer(htmlEngine, textEngine, templateVariables, properties);
    }

    @ParameterizedTest
    @EnumSource(CommunicationEmailTemplate.class)
    void rendersAllCatalogTemplates(CommunicationEmailTemplate template) {
        EmailTemplateModel model = sampleModel(template.templateName(), template.defaultSubject());

        RenderedEmail rendered = renderer.render(model);

        assertThat(rendered.htmlBody()).contains("Engineering Learning Hub");
        assertThat(rendered.htmlBody()).contains("Jane Doe");
        assertThat(rendered.htmlBody()).contains("This is an automated message");
        assertThat(rendered.textBody()).contains("Engineering Learning Hub");
        assertThat(rendered.textBody()).contains("Jane Doe");
        assertThat(rendered.textBody()).doesNotContain("[[${");
        assertThat(rendered.htmlBody()).doesNotContain("[[${");
        assertThat(rendered.subject()).isEqualTo(template.defaultSubject());
    }

    @Test
    void rendersHtmlWithBrandingAndCtaButton() {
        EmailTemplateModel model = new EmailTemplateModel(
                "certificate-approved",
                "Certificate approved",
                "Jane Doe",
                "jane@example.com",
                null,
                "Your certificate was approved.",
                "http://localhost:5173/certifications",
                "View certifications",
                properties.getFrontendBaseUrl(),
                properties.getSupportEmail(),
                "2026",
                null,
                "AWS Solutions Architect",
                null,
                null,
                null,
                CommunicationPriority.NORMAL,
                Map.of()
        );

        String html = renderer.renderHtml(model);

        assertThat(html).contains("linear-gradient(135deg, #1f5eff");
        assertThat(html).contains("class=\"cta-button\"");
        assertThat(html).contains("href=\"http://localhost:5173/certifications\"");
        assertThat(html).contains("View certifications");
        assertThat(html).contains("support@learninghub.local");
    }

    @Test
    void rendersTextWithActionLink() {
        EmailTemplateModel model = new EmailTemplateModel(
                "password-reset",
                "Reset your password",
                "Jane Doe",
                "jane@example.com",
                null,
                "We received a request to reset your password.",
                "http://localhost:5173/reset-password?token=abc",
                "Reset password",
                properties.getFrontendBaseUrl(),
                properties.getSupportEmail(),
                "2026",
                null,
                null,
                null,
                "http://localhost:5173/reset-password?token=abc",
                "60",
                CommunicationPriority.HIGH,
                Map.of()
        );

        String text = renderer.renderText(model);

        assertThat(text).contains("Reset password: http://localhost:5173/reset-password?token=abc");
        assertThat(text).contains("This link expires in 60 minutes.");
    }

    @Test
    void rendersOptionalVariablesWhenPresent() {
        EmailTemplateModel model = new EmailTemplateModel(
                "certificate-rejected",
                "Certificate requires attention",
                "Jane Doe",
                "jane@example.com",
                "Admin User",
                "Your submission needs changes.",
                "http://localhost:5173/submissions",
                "View submissions",
                properties.getFrontendBaseUrl(),
                properties.getSupportEmail(),
                "2026",
                "Platform Team",
                "AWS Solutions Architect",
                "AWS",
                null,
                null,
                CommunicationPriority.NORMAL,
                Map.of("rejectionReason", "Missing proof of completion")
        );

        RenderedEmail rendered = renderer.render(model);

        assertThat(rendered.htmlBody()).contains("Missing proof of completion");
        assertThat(rendered.textBody()).contains("Reason: Missing proof of completion");
        assertThat(rendered.htmlBody()).contains("AWS Solutions Architect");
        assertThat(rendered.htmlBody()).contains("Platform Team");
    }

    @Test
    void omitsOptionalSectionsWhenVariablesMissing() {
        EmailTemplateModel model = sampleModel("generic-notification", "Notification");

        RenderedEmail rendered = renderer.render(model);

        assertThat(rendered.htmlBody()).doesNotContain("Reason");
        assertThat(rendered.htmlBody()).doesNotContain("Submitted by");
        assertThat(rendered.textBody()).doesNotContain("Reason:");
    }

    @Test
    void buildEmailMessageUsesRenderedContent() {
        User recipient = new User("E001", "jane@example.com", "Jane Doe", "hash");
        CommunicationEvent event = new CommunicationEvent(
                UUID.randomUUID(),
                CommunicationEventType.CERTIFICATE_APPROVED,
                Instant.parse("2026-07-12T12:00:00Z"),
                UUID.randomUUID(),
                recipient.getId(),
                new CommunicationEntityRef("CERTIFICATE_SUBMISSION", UUID.randomUUID(), "/certifications"),
                Map.of("message", "Your certificate was approved."),
                Set.of(),
                CommunicationPriority.HIGH,
                "test-key"
        );

        EmailMessage message = renderer.buildEmailMessage(recipient, event);

        assertThat(message.to()).isEqualTo("jane@example.com");
        assertThat(message.from()).isEqualTo("noreply@learninghub.local");
        assertThat(message.subject()).isEqualTo("Certificate approved");
        assertThat(message.htmlBody()).contains("Your certificate was approved.");
        assertThat(message.textBody()).contains("Your certificate was approved.");
    }

    @Test
    void renderSubjectReturnsModelSubject() {
        EmailTemplateModel model = sampleModel("account-created", "Welcome aboard");

        assertThat(renderer.renderSubject(model)).isEqualTo("Welcome aboard");
    }

    private EmailTemplateModel sampleModel(String templateName, String subject) {
        return new EmailTemplateModel(
                templateName,
                subject,
                "Jane Doe",
                "jane@example.com",
                null,
                "Sample notification message.",
                null,
                null,
                properties.getFrontendBaseUrl(),
                properties.getSupportEmail(),
                "2026",
                null,
                null,
                null,
                null,
                null,
                CommunicationPriority.NORMAL,
                Map.of()
        );
    }
}
