package com.company.learninghub.communication.repository;

import com.company.learninghub.communication.domain.CommunicationOutboxEntry;
import com.company.learninghub.communication.domain.CommunicationOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommunicationOutboxRepository extends JpaRepository<CommunicationOutboxEntry, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query(value = """
            SELECT *
            FROM communication_outbox
            WHERE status IN ('PENDING', 'FAILED')
              AND available_at <= :now
            ORDER BY
              CASE priority
                WHEN 'HIGH' THEN 1
                WHEN 'NORMAL' THEN 2
                WHEN 'LOW' THEN 3
                ELSE 4
              END,
              created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<CommunicationOutboxEntry> lockNextBatch(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );

    long countByStatusIn(List<CommunicationOutboxStatus> statuses);
}
