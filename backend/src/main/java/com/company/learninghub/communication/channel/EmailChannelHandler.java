package com.company.learninghub.communication.channel;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.domain.CommunicationOutboxEntry;
import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import com.company.learninghub.communication.email.EmailDeliveryResult;
import com.company.learninghub.communication.email.EmailMessage;
import com.company.learninghub.communication.email.EmailProvider;
import com.company.learninghub.communication.repository.CommunicationOutboxRepository;
import com.company.learninghub.communication.service.CommunicationEventSerializer;
import com.company.learninghub.communication.template.EmailTemplateRenderer;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class EmailChannelHandler {

    private final CommunicationOutboxRepository outboxRepository;
    private final CommunicationEventSerializer eventSerializer;
    private final CommunicationProperties communicationProperties;
    private final EmailProvider emailProvider;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final UserRepository userRepository;
    private final Clock clock;

    public EmailChannelHandler(
            CommunicationOutboxRepository outboxRepository,
            CommunicationEventSerializer eventSerializer,
            CommunicationProperties communicationProperties,
            EmailProvider emailProvider,
            EmailTemplateRenderer emailTemplateRenderer,
            UserRepository userRepository
    ) {
        this(
                outboxRepository,
                eventSerializer,
                communicationProperties,
                emailProvider,
                emailTemplateRenderer,
                userRepository,
                Clock.systemUTC()
        );
    }

    EmailChannelHandler(
            CommunicationOutboxRepository outboxRepository,
            CommunicationEventSerializer eventSerializer,
            CommunicationProperties communicationProperties,
            EmailProvider emailProvider,
            EmailTemplateRenderer emailTemplateRenderer,
            UserRepository userRepository,
            Clock clock
    ) {
        this.outboxRepository = outboxRepository;
        this.eventSerializer = eventSerializer;
        this.communicationProperties = communicationProperties;
        this.emailProvider = emailProvider;
        this.emailTemplateRenderer = emailTemplateRenderer;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public void enqueue(CommunicationEvent event) {
        if (!StringUtils.hasText(event.idempotencyKey())) {
            throw new IllegalArgumentException("Email idempotency key is required");
        }
        String emailKey = event.idempotencyKey() + ":EMAIL";
        if (outboxRepository.existsByIdempotencyKey(emailKey)) {
            return;
        }
        CommunicationOutboxEntry entry = new CommunicationOutboxEntry(
                emailKey,
                CommunicationChannel.EMAIL,
                event.type(),
                eventSerializer.serialize(event),
                event.priority(),
                Instant.now(clock),
                Instant.now(clock)
        );
        outboxRepository.save(entry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEntry(CommunicationOutboxEntry entry) {
        Instant now = Instant.now(clock);
        entry.markProcessing();
        outboxRepository.save(entry);

        CommunicationEvent event = eventSerializer.deserialize(entry.getPayloadJson());
        User recipient = userRepository.findById(event.recipientUserId())
                .filter(User::isActive)
                .orElse(null);
        if (recipient == null || !StringUtils.hasText(recipient.getEmail())) {
            entry.markDead("Recipient is inactive or has no email address", now);
            outboxRepository.save(entry);
            return;
        }

        EmailMessage message = emailTemplateRenderer.buildEmailMessage(recipient, event);
        EmailDeliveryResult result = emailProvider.send(message);
        if (result.success()) {
            entry.markSent(now);
            outboxRepository.save(entry);
            return;
        }

        handleFailure(entry, result.errorMessage(), now);
    }

    private void handleFailure(CommunicationOutboxEntry entry, String errorMessage, Instant now) {
        entry.incrementRetryCount();
        if (entry.getRetryCount() >= communicationProperties.getOutbox().getMaxRetries()) {
            entry.markDead(errorMessage, now);
        } else {
            Instant nextAttempt = now.plus(communicationProperties.getOutbox().backoffForAttempt(entry.getRetryCount()));
            entry.markFailed(errorMessage, nextAttempt);
        }
        outboxRepository.save(entry);
    }
}
