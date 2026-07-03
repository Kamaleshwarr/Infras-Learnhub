CREATE TABLE learn_learning_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    technology_slug VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ,
    current_stage_id UUID,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_learning_enrollments_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_learn_learning_enrollments_technology FOREIGN KEY (technology_slug)
        REFERENCES learn_technologies (slug),
    CONSTRAINT fk_learn_learning_enrollments_current_stage FOREIGN KEY (current_stage_id)
        REFERENCES learn_roadmap_stages (id) ON DELETE SET NULL,
    CONSTRAINT chk_learn_learning_enrollments_status CHECK (
        status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'LEFT')
    )
);

CREATE UNIQUE INDEX uk_learn_learning_enrollments_active
    ON learn_learning_enrollments (user_id, technology_slug)
    WHERE status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');

CREATE INDEX idx_learn_learning_enrollments_user ON learn_learning_enrollments (user_id);
CREATE INDEX idx_learn_learning_enrollments_status ON learn_learning_enrollments (status);
CREATE INDEX idx_learn_learning_enrollments_last_activity ON learn_learning_enrollments (last_activity_at DESC);

CREATE TABLE learn_stage_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID NOT NULL,
    stage_id UUID NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_stage_progress_enrollment FOREIGN KEY (enrollment_id)
        REFERENCES learn_learning_enrollments (id) ON DELETE CASCADE,
    CONSTRAINT fk_learn_stage_progress_stage FOREIGN KEY (stage_id)
        REFERENCES learn_roadmap_stages (id) ON DELETE CASCADE,
    CONSTRAINT uk_learn_stage_progress_enrollment_stage UNIQUE (enrollment_id, stage_id)
);

CREATE INDEX idx_learn_stage_progress_enrollment ON learn_stage_progress (enrollment_id);
CREATE INDEX idx_learn_stage_progress_stage ON learn_stage_progress (stage_id);
