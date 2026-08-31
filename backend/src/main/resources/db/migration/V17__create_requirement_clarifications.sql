CREATE TABLE requirement_clarifications (
    id UUID PRIMARY KEY,
    requirement_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    question VARCHAR(10000) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response VARCHAR(10000),
    responded_by UUID,
    responded_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_clarification_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT fk_clarification_requested_by FOREIGN KEY (requested_by) REFERENCES users (id),
    CONSTRAINT fk_clarification_responded_by FOREIGN KEY (responded_by) REFERENCES users (id),
    CONSTRAINT ck_clarification_status CHECK (status IN ('OPEN', 'RESPONDED', 'RESOLVED')),
    CONSTRAINT ck_clarification_question CHECK (length(btrim(question)) > 0),
    CONSTRAINT ck_clarification_response CHECK (response IS NULL OR length(btrim(response)) > 0)
);
CREATE INDEX idx_clarification_requirement ON requirement_clarifications(requirement_id, requested_at DESC);
CREATE UNIQUE INDEX uq_open_clarification_per_requirement ON requirement_clarifications(requirement_id) WHERE status = 'OPEN';
