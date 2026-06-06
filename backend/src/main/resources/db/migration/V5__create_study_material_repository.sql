CREATE TABLE study_material_folders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_study_material_folders_parent FOREIGN KEY (parent_id) REFERENCES study_material_folders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_study_material_folders_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_study_material_folders_parent_name
    ON study_material_folders (COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid), LOWER(name));
CREATE INDEX idx_study_material_folders_parent ON study_material_folders (parent_id);

CREATE TABLE study_materials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folder_id UUID,
    title VARCHAR(250) NOT NULL,
    description TEXT,
    material_type VARCHAR(30) NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    storage_provider VARCHAR(50),
    storage_key VARCHAR(500),
    original_filename VARCHAR(255),
    content_type VARCHAR(150),
    file_size_bytes BIGINT,
    external_url TEXT,
    uploaded_by UUID NOT NULL,
    download_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_study_materials_folder FOREIGN KEY (folder_id) REFERENCES study_material_folders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_study_materials_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_study_materials_storage_key UNIQUE (storage_key),
    CONSTRAINT chk_study_materials_material_type CHECK (material_type IN ('PDF', 'PPT', 'DOCX', 'VIDEO_LINK', 'EXTERNAL_LINK')),
    CONSTRAINT chk_study_materials_source_type CHECK (source_type IN ('FILE', 'LINK')),
    CONSTRAINT chk_study_materials_file_source CHECK (
        (source_type = 'FILE'
            AND material_type IN ('PDF', 'PPT', 'DOCX')
            AND storage_provider IS NOT NULL
            AND storage_key IS NOT NULL
            AND original_filename IS NOT NULL
            AND content_type IS NOT NULL
            AND file_size_bytes IS NOT NULL
            AND file_size_bytes > 0
            AND external_url IS NULL)
        OR
        (source_type = 'LINK'
            AND material_type IN ('VIDEO_LINK', 'EXTERNAL_LINK')
            AND external_url IS NOT NULL
            AND storage_provider IS NULL
            AND storage_key IS NULL
            AND original_filename IS NULL
            AND content_type IS NULL
            AND file_size_bytes IS NULL)
    )
);

CREATE INDEX idx_study_materials_folder ON study_materials (folder_id);
CREATE INDEX idx_study_materials_material_type ON study_materials (material_type);
CREATE INDEX idx_study_materials_source_type ON study_materials (source_type);
CREATE INDEX idx_study_materials_uploaded_by ON study_materials (uploaded_by);
CREATE INDEX idx_study_materials_title_lower ON study_materials (LOWER(title));

CREATE TABLE study_material_download_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL,
    downloaded_by UUID NOT NULL,
    downloaded_at_utc TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_study_material_download_events_material FOREIGN KEY (material_id) REFERENCES study_materials (id) ON DELETE CASCADE,
    CONSTRAINT fk_study_material_download_events_downloaded_by FOREIGN KEY (downloaded_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_study_material_download_events_material ON study_material_download_events (material_id);
CREATE INDEX idx_study_material_download_events_downloaded_by ON study_material_download_events (downloaded_by);
CREATE INDEX idx_study_material_download_events_downloaded_at ON study_material_download_events (downloaded_at_utc);

