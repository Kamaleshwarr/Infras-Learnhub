CREATE TABLE learn_roadmaps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    technology_slug VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    description TEXT,
    source VARCHAR(100),
    source_url VARCHAR(2048),
    catalog_updated_at TIMESTAMPTZ,
    catalog_present BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_roadmaps_technology_slug FOREIGN KEY (technology_slug)
        REFERENCES learn_technologies (slug) ON DELETE CASCADE,
    CONSTRAINT uk_learn_roadmaps_technology_slug UNIQUE (technology_slug)
);

CREATE INDEX idx_learn_roadmaps_technology_slug ON learn_roadmaps (technology_slug);
CREATE INDEX idx_learn_roadmaps_catalog_present ON learn_roadmaps (catalog_present);

CREATE TABLE learn_roadmap_stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id UUID NOT NULL,
    stage_order INTEGER NOT NULL,
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    estimated_effort VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_roadmap_stages_roadmap FOREIGN KEY (roadmap_id)
        REFERENCES learn_roadmaps (id) ON DELETE CASCADE,
    CONSTRAINT uk_learn_roadmap_stages_roadmap_slug UNIQUE (roadmap_id, slug),
    CONSTRAINT uk_learn_roadmap_stages_roadmap_order UNIQUE (roadmap_id, stage_order),
    CONSTRAINT chk_learn_roadmap_stages_order CHECK (stage_order >= 1)
);

CREATE INDEX idx_learn_roadmap_stages_roadmap ON learn_roadmap_stages (roadmap_id);

CREATE TABLE learn_roadmap_stage_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id UUID NOT NULL,
    resource_kind VARCHAR(20) NOT NULL,
    resource_order INTEGER NOT NULL DEFAULT 0,
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    provider VARCHAR(100),
    free_paid VARCHAR(20),
    version VARCHAR(20),
    source VARCHAR(100),
    updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_roadmap_stage_resources_stage FOREIGN KEY (stage_id)
        REFERENCES learn_roadmap_stages (id) ON DELETE CASCADE,
    CONSTRAINT uk_learn_roadmap_stage_resources_stage_slug UNIQUE (stage_id, slug),
    CONSTRAINT chk_learn_roadmap_stage_resources_kind CHECK (resource_kind IN ('LEARNING', 'PRACTICE')),
    CONSTRAINT chk_learn_roadmap_stage_resources_free_paid CHECK (
        free_paid IS NULL OR free_paid IN ('FREE', 'PAID', 'FREEMIUM')
    )
);

CREATE INDEX idx_learn_roadmap_stage_resources_stage ON learn_roadmap_stage_resources (stage_id);
