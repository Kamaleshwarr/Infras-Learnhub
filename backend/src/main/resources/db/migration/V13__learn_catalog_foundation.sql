ALTER TABLE learn_technologies
    ADD COLUMN slug VARCHAR(100),
    ADD COLUMN estimated_duration VARCHAR(50),
    ADD COLUMN official_website VARCHAR(2048),
    ADD COLUMN official_documentation VARCHAR(2048),
    ADD COLUMN tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN org_notes TEXT,
    ADD COLUMN catalog_version VARCHAR(20),
    ADD COLUMN catalog_source VARCHAR(100),
    ADD COLUMN catalog_source_url VARCHAR(2048),
    ADD COLUMN catalog_updated_at TIMESTAMPTZ,
    ADD COLUMN featured_override BOOLEAN,
    ADD COLUMN catalog_present BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE learn_technologies
SET status = 'HIDDEN'
WHERE status = 'DRAFT';

UPDATE learn_technologies
SET slug = 'aws'
WHERE LOWER(name) LIKE '%amazon web services%'
   OR LOWER(short_name) = 'aws';

UPDATE learn_technologies
SET slug = 'spring-boot'
WHERE LOWER(name) LIKE '%spring boot%';

UPDATE learn_technologies
SET slug = 'kubernetes'
WHERE LOWER(short_name) = 'k8s'
   OR LOWER(name) LIKE '%kubernetes%';

UPDATE learn_technologies
SET category = 'BACKEND'
WHERE category = 'LANGUAGES';

UPDATE learn_technologies
SET category = 'DATA_ENGINEERING'
WHERE category = 'DATA';

UPDATE learn_technologies
SET category = 'ARCHITECTURE'
WHERE category IN ('PLATFORM', 'OTHER');

UPDATE learn_technologies
SET slug = LOWER(REGEXP_REPLACE(REGEXP_REPLACE(TRIM(name), '[^a-zA-Z0-9]+', '-', 'g'), '(^-|-$)', '', 'g'))
WHERE slug IS NULL;

ALTER TABLE learn_technologies
    RENAME COLUMN featured TO catalog_featured;

ALTER TABLE learn_technologies
    ALTER COLUMN slug SET NOT NULL;

ALTER TABLE learn_technologies
    DROP CONSTRAINT IF EXISTS chk_learn_technologies_category;

ALTER TABLE learn_technologies
    DROP CONSTRAINT IF EXISTS chk_learn_technologies_status;

DROP INDEX IF EXISTS uk_learn_technologies_name_lower;

CREATE UNIQUE INDEX uk_learn_technologies_slug ON learn_technologies (slug);

ALTER TABLE learn_technologies
    ADD CONSTRAINT chk_learn_technologies_category CHECK (category IN (
        'BACKEND',
        'FRONTEND',
        'CLOUD',
        'DEVOPS',
        'DATABASE',
        'AI_AND_GENAI',
        'TESTING',
        'SECURITY',
        'MOBILE',
        'ARCHITECTURE',
        'DATA_ENGINEERING'
    ));

ALTER TABLE learn_technologies
    ADD CONSTRAINT chk_learn_technologies_status CHECK (status IN ('HIDDEN', 'PUBLISHED', 'ARCHIVED'));

CREATE TABLE learn_catalog_imports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_version VARCHAR(20) NOT NULL,
    imported_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    package_type VARCHAR(50) NOT NULL,
    records_upserted INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_learn_catalog_imports_status CHECK (status IN ('SUCCESS', 'FAILED'))
);

CREATE INDEX idx_learn_catalog_imports_version ON learn_catalog_imports (catalog_version);
