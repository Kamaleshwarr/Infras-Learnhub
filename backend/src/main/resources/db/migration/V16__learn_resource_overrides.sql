CREATE TABLE learn_stage_resource_overrides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    technology_slug VARCHAR(100) NOT NULL,
    stage_slug VARCHAR(100) NOT NULL,
    resource_slug VARCHAR(100) NOT NULL,
    catalog_resource_slug VARCHAR(100),
    resource_kind VARCHAR(20) NOT NULL,
    disabled BOOLEAN NOT NULL DEFAULT FALSE,
    override_url VARCHAR(2048),
    preferred BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reason TEXT,
    title VARCHAR(200),
    resource_type VARCHAR(50),
    provider VARCHAR(100),
    free_paid VARCHAR(20),
    resource_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_learn_resource_override_kind CHECK (resource_kind IN ('LEARNING', 'PRACTICE')),
    CONSTRAINT chk_learn_resource_override_free_paid CHECK (
        free_paid IS NULL OR free_paid IN ('FREE', 'PAID', 'FREEMIUM')
    )
);

CREATE UNIQUE INDEX uk_learn_stage_resource_overrides_slug
    ON learn_stage_resource_overrides (technology_slug, stage_slug, resource_slug);

CREATE INDEX idx_learn_stage_resource_overrides_technology
    ON learn_stage_resource_overrides (technology_slug);

CREATE INDEX idx_learn_stage_resource_overrides_stage
    ON learn_stage_resource_overrides (technology_slug, stage_slug);
