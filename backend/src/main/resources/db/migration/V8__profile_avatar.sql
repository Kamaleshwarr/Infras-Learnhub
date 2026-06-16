ALTER TABLE users
    ADD COLUMN avatar_storage_provider VARCHAR(20),
    ADD COLUMN avatar_storage_key VARCHAR(500),
    ADD COLUMN avatar_content_type VARCHAR(100),
    ADD COLUMN avatar_original_filename VARCHAR(255),
    ADD COLUMN avatar_file_size_bytes BIGINT,
    ADD COLUMN avatar_updated_at TIMESTAMPTZ;

CREATE INDEX idx_users_avatar_updated_at ON users (avatar_updated_at)
    WHERE avatar_storage_key IS NOT NULL;
