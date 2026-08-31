CREATE TABLE user_stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id UUID NOT NULL,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(10000),
    story_text VARCHAR(10000) NOT NULL,
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_stories_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_stories_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_user_stories_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_user_stories_status CHECK (status IN ('DRAFT', 'READY', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX idx_user_stories_requirement_id ON user_stories (requirement_id);
CREATE INDEX idx_user_stories_created_by ON user_stories (created_by);
CREATE INDEX idx_user_stories_status ON user_stories (status);
CREATE INDEX idx_user_stories_priority ON user_stories (priority);
