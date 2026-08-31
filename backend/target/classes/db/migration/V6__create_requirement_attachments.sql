CREATE TABLE requirement_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id UUID NOT NULL,
    uploaded_by UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(320) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requirement_attachments_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT fk_requirement_attachments_user FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_requirement_attachments_file_size CHECK (file_size > 0)
);

CREATE INDEX idx_requirement_attachments_requirement_created ON requirement_attachments (requirement_id, created_at DESC);
CREATE INDEX idx_requirement_attachments_uploaded_by ON requirement_attachments (uploaded_by);