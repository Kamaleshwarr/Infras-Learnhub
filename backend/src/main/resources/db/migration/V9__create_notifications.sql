CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    type        VARCHAR(60) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     TEXT NOT NULL,
    entity_type VARCHAR(60),
    entity_id   UUID,
    action_path VARCHAR(500),
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_notifications_type CHECK (type IN (
        'CERTIFICATE_SUBMITTED',
        'CERTIFICATE_APPROVED',
        'CERTIFICATE_REJECTED',
        'PASSWORD_RESET_BY_ADMIN',
        'ACCOUNT_ACTIVATED',
        'ACCOUNT_DEACTIVATED',
        'ACCOUNT_CREATED'
    ))
);

CREATE INDEX idx_notifications_user_created_at ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read_at) WHERE read_at IS NULL;
