ALTER TABLE project_members
    ADD COLUMN functional_role VARCHAR(40) NOT NULL DEFAULT 'OTHER',
    ADD COLUMN responsibility TEXT,
    ADD COLUMN is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN display_order INT NOT NULL DEFAULT 0;

ALTER TABLE project_members
    ADD CONSTRAINT chk_project_members_functional_role CHECK (functional_role IN (
        'PRODUCT_OWNER',
        'PROJECT_MANAGER',
        'BUSINESS_ANALYST',
        'TECH_LEAD',
        'DEVELOPER',
        'QA_ENGINEER',
        'QA_LEAD',
        'AUTOMATION_ENGINEER',
        'DEVOPS_ENGINEER',
        'UI_UX_DESIGNER',
        'ARCHITECT',
        'SCRUM_MASTER',
        'SUPPORT',
        'OTHER'
    ));

CREATE INDEX idx_project_members_functional_role ON project_members (project_id, functional_role);
CREATE INDEX idx_project_members_primary_contact ON project_members (project_id, is_primary_contact);
CREATE INDEX idx_project_members_display_order ON project_members (project_id, display_order, functional_role);

CREATE TABLE project_external_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_type VARCHAR(40) NOT NULL,
    role_title VARCHAR(200),
    organization VARCHAR(200),
    email VARCHAR(320),
    phone VARCHAR(50),
    contact_url TEXT,
    notes TEXT,
    is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_external_contacts_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_external_contacts_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_project_external_contacts_type CHECK (contact_type IN (
        'CLIENT',
        'VENDOR',
        'BUSINESS',
        'INFRASTRUCTURE',
        'SECURITY',
        'SUPPORT',
        'OTHER'
    ))
);

CREATE INDEX idx_project_external_contacts_project ON project_external_contacts (project_id);
CREATE INDEX idx_project_external_contacts_active ON project_external_contacts (active);
CREATE INDEX idx_project_external_contacts_display_order ON project_external_contacts (project_id, display_order, name);
