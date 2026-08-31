CREATE TABLE developer_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    requirement_id UUID,
    user_story_id UUID,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(10000),
    assigned_to UUID,
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date DATE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_developer_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_developer_tasks_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE SET NULL,
    CONSTRAINT fk_developer_tasks_user_story FOREIGN KEY (user_story_id) REFERENCES user_stories (id) ON DELETE SET NULL,
    CONSTRAINT fk_developer_tasks_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_developer_tasks_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_developer_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_developer_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'CODE_REVIEW', 'COMPLETED', 'BLOCKED'))
);

CREATE INDEX idx_developer_tasks_project_id ON developer_tasks (project_id);
CREATE INDEX idx_developer_tasks_requirement_id ON developer_tasks (requirement_id);
CREATE INDEX idx_developer_tasks_user_story_id ON developer_tasks (user_story_id);
CREATE INDEX idx_developer_tasks_assigned_to ON developer_tasks (assigned_to);
CREATE INDEX idx_developer_tasks_status ON developer_tasks (status);
CREATE INDEX idx_developer_tasks_priority ON developer_tasks (priority);
