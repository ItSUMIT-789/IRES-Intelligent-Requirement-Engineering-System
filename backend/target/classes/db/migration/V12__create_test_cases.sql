CREATE TABLE test_cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    requirement_id UUID,
    user_story_id UUID,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(10000),
    preconditions VARCHAR(10000),
    expected_result VARCHAR(10000) NOT NULL,
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    assigned_to UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_cases_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_test_cases_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE SET NULL,
    CONSTRAINT fk_test_cases_user_story FOREIGN KEY (user_story_id) REFERENCES user_stories (id) ON DELETE SET NULL,
    CONSTRAINT fk_test_cases_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_test_cases_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_test_cases_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_test_cases_status CHECK (status IN ('DRAFT', 'READY', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX idx_test_cases_project_id ON test_cases (project_id);
CREATE INDEX idx_test_cases_requirement_id ON test_cases (requirement_id);
CREATE INDEX idx_test_cases_user_story_id ON test_cases (user_story_id);
CREATE INDEX idx_test_cases_assigned_to ON test_cases (assigned_to);
CREATE INDEX idx_test_cases_status ON test_cases (status);
CREATE INDEX idx_test_cases_priority ON test_cases (priority);

CREATE TABLE test_case_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_case_id UUID NOT NULL,
    executed_by UUID NOT NULL,
    execution_status VARCHAR(10) NOT NULL DEFAULT 'NOT_RUN',
    actual_result VARCHAR(10000),
    executed_at TIMESTAMPTZ,
    notes VARCHAR(10000),
    CONSTRAINT fk_test_case_executions_test_case FOREIGN KEY (test_case_id) REFERENCES test_cases (id) ON DELETE CASCADE,
    CONSTRAINT fk_test_case_executions_user FOREIGN KEY (executed_by) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_test_case_executions_status CHECK (execution_status IN ('NOT_RUN', 'PASS', 'FAIL', 'BLOCKED'))
);

CREATE INDEX idx_test_case_executions_test_case_id ON test_case_executions (test_case_id);
CREATE INDEX idx_test_case_executions_executed_by ON test_case_executions (executed_by);
CREATE INDEX idx_test_case_executions_executed_at ON test_case_executions (executed_at DESC);
