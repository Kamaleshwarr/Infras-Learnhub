package com.company.learninghub.communication.email;

import com.company.learninghub.communication.config.CommunicationProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(prefix = "app.communication.email", name = "provider", havingValue = "smtp")
public class SmtpEmailProvider implements EmailProvider {

    private final CommunicationProperties communicationProperties;
    private final JavaMailSender javaMailSender;

    public SmtpEmailProvider(
            CommunicationProperties communicationProperties,
            @Autowired(required = false) JavaMailSender javaMailSender
    ) {
        this.communicationProperties = communicationProperties;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public EmailDeliveryResult send(EmailMessage message) {
        if (javaMailSender == null) {
            return EmailDeliveryResult.failure("SMTP mail sender is not configured");
        }
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(message.from());
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            if (message.htmlBody() != null && !message.htmlBody().isBlank()) {
                helper.setText(message.textBody(), message.htmlBody());
            } else {
                helper.setText(message.textBody(), false);
            }
            javaMailSender.send(mimeMessage);
            return EmailDeliveryResult.success(mimeMessage.getMessageID());
        } catch (MessagingException ex) {
            return EmailDeliveryResult.failure(ex.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        return javaMailSender != null;
    }

    @Override
    public String providerName() {
        return "smtp";
    }
}
