CREATE TABLE certificate_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    storage_provider VARCHAR(50) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_certificate_documents_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_certificate_documents_storage_key UNIQUE (storage_key),
    CONSTRAINT chk_certificate_documents_file_size CHECK (file_size_bytes > 0)
);

CREATE TABLE certificate_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL,
    initiative_id UUID NOT NULL,
    certificate_document_id UUID NOT NULL,
    comments TEXT,
    submitted_at_utc TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approval_status VARCHAR(20) NOT NULL,
    reviewed_by UUID,
    reviewed_at_utc TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_certificate_submissions_employee FOREIGN KEY (employee_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_certificate_submissions_initiative FOREIGN KEY (initiative_id) REFERENCES learning_initiatives (id) ON DELETE RESTRICT,
    CONSTRAINT fk_certificate_submissions_document FOREIGN KEY (certificate_document_id) REFERENCES certificate_documents (id) ON DELETE RESTRICT,
    CONSTRAINT fk_certificate_submissions_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_certificate_submissions_employee_initiative UNIQUE (employee_id, initiative_id),
    CONSTRAINT chk_certificate_submissions_status CHECK (approval_status IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_certificate_submissions_rejection_reason CHECK (
        approval_status <> 'REJECTED'
        OR (rejection_reason IS NOT NULL AND LENGTH(TRIM(rejection_reason)) > 0)
    ),
    CONSTRAINT chk_certificate_submissions_review_metadata CHECK (
        (approval_status = 'SUBMITTED' AND reviewed_by IS NULL AND reviewed_at_utc IS NULL)
        OR (approval_status IN ('APPROVED', 'REJECTED') AND reviewed_by IS NOT NULL AND reviewed_at_utc IS NOT NULL)
    )
);

CREATE INDEX idx_certificate_documents_uploaded_by ON certificate_documents (uploaded_by);
CREATE INDEX idx_certificate_submissions_employee ON certificate_submissions (employee_id);
CREATE INDEX idx_certificate_submissions_initiative ON certificate_submissions (initiative_id);
CREATE INDEX idx_certificate_submissions_status ON certificate_submissions (approval_status);
CREATE INDEX idx_certificate_submissions_submitted_at ON certificate_submissions (submitted_at_utc);
CREATE INDEX idx_certificate_submissions_reviewed_at ON certificate_submissions (reviewed_at_utc);

