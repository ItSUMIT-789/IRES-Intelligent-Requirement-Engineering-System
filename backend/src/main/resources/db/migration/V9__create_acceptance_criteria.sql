CREATE TABLE acceptance_criteria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id UUID NOT NULL,
    user_story_id UUID,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(10000),
    criteria_type VARCHAR(20) NOT NULL DEFAULT 'FUNCTIONAL',
    status VARCHAR(10) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acceptance_criteria_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT fk_acceptance_criteria_user_story FOREIGN KEY (user_story_id) REFERENCES user_stories (id) ON DELETE SET NULL,
    CONSTRAINT ck_acceptance_criteria_type CHECK (criteria_type IN ('FUNCTIONAL', 'BEHAVIORAL', 'VALIDATION')),
    CONSTRAINT ck_acceptance_criteria_status CHECK (status IN ('DRAFT', 'READY', 'PASSED', 'FAILED'))
);

CREATE INDEX idx_acceptance_criteria_requirement_id ON acceptance_criteria (requirement_id);
CREATE INDEX idx_acceptance_criteria_user_story_id ON acceptance_criteria (user_story_id);
CREATE INDEX idx_acceptance_criteria_status ON acceptance_criteria (status);
