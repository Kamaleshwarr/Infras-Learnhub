CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    access_type VARCHAR(30) NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_projects_access_type CHECK (access_type IN ('PUBLIC', 'MEMBERS_ONLY')),
    CONSTRAINT uk_projects_name UNIQUE (name)
);

CREATE INDEX idx_projects_access_type ON projects (access_type);
CREATE INDEX idx_projects_archived ON projects (archived);
CREATE INDEX idx_projects_name_lower ON projects (LOWER(name));

CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    project_role VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_project_members_role CHECK (project_role IN ('OWNER', 'CONTRIBUTOR', 'VIEWER')),
    CONSTRAINT uk_project_members_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_members_project ON project_members (project_id);
CREATE INDEX idx_project_members_user ON project_members (user_id);
CREATE INDEX idx_project_members_role ON project_members (project_role);

CREATE TABLE project_knowledge_folders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_knowledge_folders_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_knowledge_folders_parent FOREIGN KEY (parent_id) REFERENCES project_knowledge_folders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_project_knowledge_folders_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_project_knowledge_folders_project_parent_name
    ON project_knowledge_folders (
        project_id,
        COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid),
        LOWER(name)
    );
CREATE INDEX idx_project_knowledge_folders_project ON project_knowledge_folders (project_id);
CREATE INDEX idx_project_knowledge_folders_parent ON project_knowledge_folders (parent_id);

CREATE TABLE project_knowledge_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    folder_id UUID,
    title VARCHAR(250) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    storage_provider VARCHAR(50),
    storage_key VARCHAR(500),
    original_filename VARCHAR(255),
    content_type VARCHAR(150),
    file_size_bytes BIGINT,
    external_url TEXT,
    uploaded_by UUID NOT NULL,
    access_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_knowledge_items_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_knowledge_items_folder FOREIGN KEY (folder_id) REFERENCES project_knowledge_folders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_project_knowledge_items_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_project_knowledge_items_storage_key UNIQUE (storage_key),
    CONSTRAINT chk_project_knowledge_items_category CHECK (category IN (
        'REQUIREMENTS',
        'KT_DOCUMENTS',
        'ARCHITECTURE_DOCUMENTS',
        'RELEASE_NOTES',
        'TEST_STRATEGY',
        'TEST_DATA_DOCUMENTATION',
        'KT_VIDEOS',
        'EXTERNAL_LINKS'
    )),
    CONSTRAINT chk_project_knowledge_items_source_type CHECK (source_type IN ('FILE', 'LINK')),
    CONSTRAINT chk_project_knowledge_items_source CHECK (
        (source_type = 'FILE'
            AND storage_provider IS NOT NULL
            AND storage_key IS NOT NULL
            AND original_filename IS NOT NULL
            AND content_type IS NOT NULL
            AND file_size_bytes IS NOT NULL
            AND file_size_bytes > 0
            AND external_url IS NULL)
        OR
        (source_type = 'LINK'
            AND external_url IS NOT NULL
            AND storage_provider IS NULL
            AND storage_key IS NULL
            AND original_filename IS NULL
            AND content_type IS NULL
            AND file_size_bytes IS NULL)
    )
);

CREATE INDEX idx_project_knowledge_items_project ON project_knowledge_items (project_id);
CREATE INDEX idx_project_knowledge_items_folder ON project_knowledge_items (folder_id);
CREATE INDEX idx_project_knowledge_items_category ON project_knowledge_items (category);
CREATE INDEX idx_project_knowledge_items_source_type ON project_knowledge_items (source_type);
CREATE INDEX idx_project_knowledge_items_title_lower ON project_knowledge_items (LOWER(title));

CREATE TABLE project_knowledge_access_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id UUID NOT NULL,
    accessed_by UUID NOT NULL,
    accessed_at_utc TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_knowledge_access_events_item FOREIGN KEY (item_id) REFERENCES project_knowledge_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_knowledge_access_events_accessed_by FOREIGN KEY (accessed_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_project_knowledge_access_events_item ON project_knowledge_access_events (item_id);
CREATE INDEX idx_project_knowledge_access_events_accessed_by ON project_knowledge_access_events (accessed_by);
CREATE INDEX idx_project_knowledge_access_events_accessed_at ON project_knowledge_access_events (accessed_at_utc);

