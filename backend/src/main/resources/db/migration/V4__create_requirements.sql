CREATE TABLE requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(10000),
    requirement_type VARCHAR(30) NOT NULL DEFAULT 'FUNCTIONAL',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    source VARCHAR(100),
    created_by UUID NOT NULL,
    assigned_to UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requirements_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_requirements_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_requirements_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id),
    CONSTRAINT ck_requirements_type CHECK (requirement_type IN ('FUNCTIONAL', 'NON_FUNCTIONAL', 'BUSINESS', 'TECHNICAL')),
    CONSTRAINT ck_requirements_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_requirements_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'IMPLEMENTED', 'TESTED'))
);

CREATE INDEX idx_requirements_project_id ON requirements (project_id);
CREATE INDEX idx_requirements_created_by ON requirements (created_by);
CREATE INDEX idx_requirements_assigned_to ON requirements (assigned_to);
CREATE INDEX idx_requirements_status ON requirements (status);
CREATE INDEX idx_requirements_priority ON requirements (priority);
CREATE INDEX idx_requirements_title_lower ON requirements (LOWER(title));
