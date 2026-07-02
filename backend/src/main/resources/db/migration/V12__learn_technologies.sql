CREATE TABLE learn_technologies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(30) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_technologies_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_learn_technologies_category CHECK (category IN (
        'CLOUD',
        'LANGUAGES',
        'DEVOPS',
        'DATA',
        'SECURITY',
        'PLATFORM',
        'OTHER'
    )),
    CONSTRAINT chk_learn_technologies_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT chk_learn_technologies_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uk_learn_technologies_name_lower ON learn_technologies (LOWER(name));
CREATE INDEX idx_learn_technologies_status ON learn_technologies (status);
CREATE INDEX idx_learn_technologies_category ON learn_technologies (category);
CREATE INDEX idx_learn_technologies_difficulty ON learn_technologies (difficulty);
CREATE INDEX idx_learn_technologies_featured ON learn_technologies (featured);

CREATE TABLE learn_technology_project_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    technology_id UUID NOT NULL,
    project_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learn_technology_project_links_technology FOREIGN KEY (technology_id) REFERENCES learn_technologies (id) ON DELETE CASCADE,
    CONSTRAINT fk_learn_technology_project_links_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT uk_learn_technology_project_links UNIQUE (technology_id, project_id)
);

CREATE INDEX idx_learn_technology_project_links_technology ON learn_technology_project_links (technology_id);
CREATE INDEX idx_learn_technology_project_links_project ON learn_technology_project_links (project_id);

INSERT INTO learn_technologies (name, short_name, description, category, difficulty, status, featured, created_by)
SELECT
    'Amazon Web Services (AWS)',
    'AWS',
    'Cloud computing platform covering compute, storage, networking, and managed services.',
    'CLOUD',
    'INTERMEDIATE',
    'PUBLISHED',
    TRUE,
    u.id
FROM users u
WHERE u.email = 'admin@learninghub.local'
ON CONFLICT DO NOTHING;

INSERT INTO learn_technologies (name, short_name, description, category, difficulty, status, featured, created_by)
SELECT
    'Spring Boot',
    'Spring Boot',
    'Java framework for building production-ready applications with minimal configuration.',
    'LANGUAGES',
    'INTERMEDIATE',
    'PUBLISHED',
    TRUE,
    u.id
FROM users u
WHERE u.email = 'admin@learninghub.local'
ON CONFLICT DO NOTHING;

INSERT INTO learn_technologies (name, short_name, description, category, difficulty, status, featured, created_by)
SELECT
    'Kubernetes',
    'K8s',
    'Container orchestration platform for deploying and managing containerized workloads.',
    'DEVOPS',
    'ADVANCED',
    'DRAFT',
    FALSE,
    u.id
FROM users u
WHERE u.email = 'admin@learninghub.local'
ON CONFLICT DO NOTHING;
