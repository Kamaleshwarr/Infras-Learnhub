CREATE TABLE project_environments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_environments_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_environments_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_project_environments_project_name_lower
    ON project_environments (project_id, LOWER(name));
CREATE INDEX idx_project_environments_project ON project_environments (project_id);
CREATE INDEX idx_project_environments_active ON project_environments (active);
CREATE INDEX idx_project_environments_display_order ON project_environments (project_id, display_order, name);

CREATE TABLE project_environment_references (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    environment_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    reference_type VARCHAR(40) NOT NULL,
    url TEXT NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_environment_references_environment FOREIGN KEY (environment_id) REFERENCES project_environments (id) ON DELETE CASCADE,
    CONSTRAINT chk_project_environment_references_type CHECK (reference_type IN (
        'APPLICATION',
        'API_BASE',
        'SWAGGER',
        'ADMIN_PORTAL',
        'EMPLOYEE_PORTAL',
        'AGENT_PORTAL',
        'MONITORING',
        'LOGS',
        'CICD_PIPELINE',
        'DEPLOYMENT',
        'API_GATEWAY',
        'DATABASE_ADMIN',
        'OTHER'
    ))
);

CREATE INDEX idx_project_environment_references_environment ON project_environment_references (environment_id);
CREATE INDEX idx_project_environment_references_type ON project_environment_references (reference_type);
CREATE INDEX idx_project_environment_references_display_order ON project_environment_references (environment_id, display_order, name);
CREATE INDEX idx_project_environment_references_name_lower ON project_environment_references (LOWER(name));

CREATE TABLE project_repositories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    repository_type VARCHAR(40) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    repository_url TEXT NOT NULL,
    default_branch VARCHAR(200),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_repositories_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_repositories_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_project_repositories_type CHECK (repository_type IN (
        'BACKEND',
        'FRONTEND',
        'FULL_STACK',
        'MOBILE',
        'AUTOMATION',
        'INFRASTRUCTURE',
        'DATABASE',
        'PERFORMANCE_TESTING',
        'SHARED_LIBRARY',
        'DOCUMENTATION',
        'OTHER'
    )),
    CONSTRAINT chk_project_repositories_provider CHECK (provider IN (
        'GITHUB',
        'GITLAB',
        'BITBUCKET',
        'AZURE_DEVOPS',
        'OTHER'
    ))
);

CREATE UNIQUE INDEX uk_project_repositories_project_name_lower
    ON project_repositories (project_id, LOWER(name));
CREATE INDEX idx_project_repositories_project ON project_repositories (project_id);
CREATE INDEX idx_project_repositories_type ON project_repositories (repository_type);
CREATE INDEX idx_project_repositories_provider ON project_repositories (provider);
CREATE INDEX idx_project_repositories_active ON project_repositories (active);
CREATE INDEX idx_project_repositories_display_order ON project_repositories (project_id, display_order, name);
