CREATE TABLE learning_initiatives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    reward_description TEXT,
    start_date_utc TIMESTAMPTZ NOT NULL,
    expiry_date_utc TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learning_initiatives_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_learning_initiatives_status CHECK (status IN ('DRAFT', 'ACTIVE', 'EXPIRED')),
    CONSTRAINT chk_learning_initiatives_dates CHECK (expiry_date_utc > start_date_utc)
);

CREATE INDEX idx_learning_initiatives_status ON learning_initiatives (status);
CREATE INDEX idx_learning_initiatives_start_date ON learning_initiatives (start_date_utc);
CREATE INDEX idx_learning_initiatives_expiry_date ON learning_initiatives (expiry_date_utc);
CREATE INDEX idx_learning_initiatives_title_lower ON learning_initiatives (LOWER(title));

