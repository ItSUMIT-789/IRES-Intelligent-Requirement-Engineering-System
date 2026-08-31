CREATE TABLE requirement_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id UUID NOT NULL,
    user_id UUID NOT NULL,
    comment VARCHAR(10000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requirement_comments_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT fk_requirement_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_requirement_comments_not_blank CHECK (length(btrim(comment)) > 0)
);

CREATE INDEX idx_requirement_comments_requirement_created ON requirement_comments (requirement_id, created_at DESC);
CREATE INDEX idx_requirement_comments_user_id ON requirement_comments (user_id);
