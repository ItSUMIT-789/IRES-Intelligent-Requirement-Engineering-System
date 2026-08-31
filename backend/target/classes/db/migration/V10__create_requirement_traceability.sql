CREATE TABLE requirement_traceability_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id UUID NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id UUID NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_traceability_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT ck_traceability_source_type CHECK (source_type IN ('REQUIREMENT', 'USER_STORY', 'ACCEPTANCE_CRITERIA', 'DEVELOPER_TASK', 'TEST_CASE', 'TEST_EXECUTION')),
    CONSTRAINT ck_traceability_target_type CHECK (target_type IN ('REQUIREMENT', 'USER_STORY', 'ACCEPTANCE_CRITERIA', 'DEVELOPER_TASK', 'TEST_CASE', 'TEST_EXECUTION')),
    CONSTRAINT ck_traceability_distinct_endpoints CHECK (NOT (source_type = target_type AND source_id = target_id)),
    CONSTRAINT uq_traceability_link UNIQUE (requirement_id, source_type, source_id, target_type, target_id)
);

CREATE INDEX idx_traceability_requirement ON requirement_traceability_links (requirement_id, created_at DESC);
CREATE INDEX idx_traceability_source ON requirement_traceability_links (source_type, source_id);
CREATE INDEX idx_traceability_target ON requirement_traceability_links (target_type, target_id);
