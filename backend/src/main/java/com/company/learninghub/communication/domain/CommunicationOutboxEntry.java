package com.company.learninghub.communication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "communication_outbox")
public class CommunicationOutboxEntry {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 200)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, updatable = false, length = 20)
    private CommunicationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 60)
    private CommunicationEventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, updatable = false, columnDefinition = "jsonb")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommunicationOutboxStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private CommunicationPriority priority;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected CommunicationOutboxEntry() {
    }

    public CommunicationOutboxEntry(
            String idempotencyKey,
            CommunicationChannel channel,
            CommunicationEventType eventType,
            String payloadJson,
            CommunicationPriority priority,
            Instant availableAt,
            Instant createdAt
    ) {
        this.idempotencyKey = idempotencyKey;
        this.channel = channel;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.status = CommunicationOutboxStatus.PENDING;
        this.priority = priority == null ? CommunicationPriority.NORMAL : priority;
        this.availableAt = availableAt;
        this.retryCount = 0;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (availableAt == null) {
            availableAt = createdAt;
        }
        if (priority == null) {
            priority = CommunicationPriority.NORMAL;
        }
        if (status == null) {
            status = CommunicationOutboxStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public CommunicationChannel getChannel() {
        return channel;
    }

    public CommunicationEventType getEventType() {
        return eventType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public CommunicationOutboxStatus getStatus() {
        return status;
    }

    public CommunicationPriority getPriority() {
        return priority;
    }

    public Instant getAvailableAt() {
        return availableAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void markProcessing() {
        this.status = CommunicationOutboxStatus.PROCESSING;
    }

    public void markSent(Instant processedAt) {
        this.status = CommunicationOutboxStatus.SENT;
        this.processedAt = processedAt;
        this.lastError = null;
    }

    public void markFailed(String error, Instant nextAvailableAt) {
        this.status = CommunicationOutboxStatus.FAILED;
        this.lastError = error;
        this.availableAt = nextAvailableAt;
    }

    public void markDead(String error, Instant processedAt) {
        this.status = CommunicationOutboxStatus.DEAD;
        this.lastError = error;
        this.processedAt = processedAt;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommunicationOutboxEntry that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
