CREATE TABLE communication_outbox (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key  VARCHAR(200) NOT NULL,
    channel          VARCHAR(20) NOT NULL,
    event_type       VARCHAR(60) NOT NULL,
    payload_json     JSONB NOT NULL,
    status           VARCHAR(20) NOT NULL,
    priority         VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    available_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    retry_count      INT NOT NULL DEFAULT 0,
    last_error       TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at     TIMESTAMPTZ,
    CONSTRAINT uk_communication_outbox_idempotency UNIQUE (idempotency_key),
    CONSTRAINT chk_communication_outbox_channel CHECK (channel IN ('EMAIL')),
    CONSTRAINT chk_communication_outbox_status CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'DEAD')),
    CONSTRAINT chk_communication_outbox_priority CHECK (priority IN ('HIGH', 'NORMAL', 'LOW'))
);

CREATE INDEX idx_communication_outbox_poll
    ON communication_outbox (status, available_at, priority DESC, created_at ASC)
    WHERE status IN ('PENDING', 'FAILED');
