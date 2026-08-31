CREATE TABLE bugs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    requirement_id UUID,
    test_case_id UUID,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    severity VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reported_by UUID NOT NULL,
    assigned_to UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT fk_bugs_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_bugs_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE SET NULL,
    CONSTRAINT fk_bugs_test_case FOREIGN KEY (test_case_id) REFERENCES test_cases (id) ON DELETE SET NULL,
    CONSTRAINT fk_bugs_reported_by FOREIGN KEY (reported_by) REFERENCES users (id),
    CONSTRAINT fk_bugs_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_bugs_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_bugs_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_bugs_status CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REOPENED', 'CLOSED'))
);

CREATE INDEX idx_bugs_project_id ON bugs (project_id);
CREATE INDEX idx_bugs_requirement_id ON bugs (requirement_id);
CREATE INDEX idx_bugs_test_case_id ON bugs (test_case_id);
CREATE INDEX idx_bugs_assigned_to ON bugs (assigned_to);
CREATE INDEX idx_bugs_status ON bugs (status);
CREATE INDEX idx_bugs_severity ON bugs (severity);
