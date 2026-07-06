ALTER TABLE projects
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

UPDATE projects
SET status = 'ARCHIVED'
WHERE archived = TRUE;

ALTER TABLE projects
    ADD CONSTRAINT chk_projects_status CHECK (status IN ('ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'));

CREATE INDEX idx_projects_status ON projects (status);
