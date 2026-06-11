package com.company.learninghub.auth.service;

import com.company.learninghub.auth.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String HTML_TEMPLATE = "templates/email/forgot-password.html";
    private static final String TEXT_TEMPLATE = "templates/email/forgot-password.txt";

    private final MailProperties mailProperties;
    private final JavaMailSender javaMailSender;

    public EmailService(
            MailProperties mailProperties,
            @Autowired(required = false) JavaMailSender javaMailSender
    ) {
        this.mailProperties = mailProperties;
        this.javaMailSender = javaMailSender;
    }

    public void sendPasswordResetEmail(String recipientEmail, String fullName, String resetUrl, Duration expiration) {
        Map<String, String> values = Map.of(
                "fullName", fullName,
                "resetUrl", resetUrl,
                "expirationMinutes", String.valueOf(expiration.toMinutes())
        );

        if (mailProperties.isLogMode()) {
            LOGGER.info(
                    "Password reset email (log mode). recipient={}, resetUrl={}, expiresInMinutes={}",
                    recipientEmail,
                    resetUrl,
                    expiration.toMinutes()
            );
            return;
        }

        if (javaMailSender == null) {
            throw new IllegalStateException("SMTP mail sender is not configured. Set spring.mail.* properties and APP_MAIL_MODE=smtp.");
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(recipientEmail);
            helper.setSubject("Reset your Learning Hub password");
            helper.setText(renderTemplate(TEXT_TEMPLATE, values), renderTemplate(HTML_TEMPLATE, values));
            javaMailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Unable to send password reset email", ex);
        }
    }

    private String renderTemplate(String classpathLocation, Map<String, String> values) {
        try {
            String template = StreamUtils.copyToString(
                    new ClassPathResource(classpathLocation).getInputStream(),
                    StandardCharsets.UTF_8
            );
            String rendered = template;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return rendered;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load email template: " + classpathLocation, ex);
        }
    }
}
