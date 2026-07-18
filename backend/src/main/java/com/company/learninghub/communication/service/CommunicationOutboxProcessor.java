package com.company.learninghub.communication.service;

import com.company.learninghub.communication.channel.EmailChannelHandler;
import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationOutboxEntry;
import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import com.company.learninghub.communication.repository.CommunicationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class CommunicationOutboxProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationOutboxProcessor.class);

    private final CommunicationOutboxRepository outboxRepository;
    private final EmailChannelHandler emailChannelHandler;
    private final CommunicationProperties communicationProperties;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    @Autowired
    public CommunicationOutboxProcessor(
            CommunicationOutboxRepository outboxRepository,
            EmailChannelHandler emailChannelHandler,
            CommunicationProperties communicationProperties,
            TransactionTemplate transactionTemplate
    ) {
        this(outboxRepository, emailChannelHandler, communicationProperties, transactionTemplate, Clock.systemUTC());
    }

    CommunicationOutboxProcessor(
            CommunicationOutboxRepository outboxRepository,
            EmailChannelHandler emailChannelHandler,
            CommunicationProperties communicationProperties,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        this.outboxRepository = outboxRepository;
        this.emailChannelHandler = emailChannelHandler;
        this.communicationProperties = communicationProperties;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.communication.outbox.poll-interval:PT10S}")
    public void processOutbox() {
        if (!communicationProperties.isEnabled()) {
            return;
        }
        Instant now = Instant.now(clock);
        int batchSize = communicationProperties.getOutbox().getBatchSize();
        List<CommunicationOutboxEntry> batch = transactionTemplate.execute(status ->
                outboxRepository.lockNextBatch(now, batchSize)
        );
        if (batch == null || batch.isEmpty()) {
            return;
        }
        for (CommunicationOutboxEntry entry : batch) {
            try {
                emailChannelHandler.processEntry(entry);
            } catch (RuntimeException ex) {
                LOGGER.warn("Failed to process communication outbox entry {}", entry.getId(), ex);
            }
        }
    }

    public long pendingCount() {
        return outboxRepository.countByStatusIn(List.of(
                CommunicationOutboxStatus.PENDING,
                CommunicationOutboxStatus.FAILED
        ));
    }
}
