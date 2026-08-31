CREATE TABLE requirement_ai_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id UUID NOT NULL,
    analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    summary TEXT,
    ambiguity_score NUMERIC(5, 2),
    completeness_score NUMERIC(5, 2),
    quality_score NUMERIC(5, 2),
    suggestions TEXT,
    analyzed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_requirement_ai_analysis_requirement UNIQUE (requirement_id),
    CONSTRAINT fk_requirement_ai_analysis_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id) ON DELETE CASCADE,
    CONSTRAINT ck_requirement_ai_analysis_status CHECK (analysis_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_requirement_ai_analysis_scores CHECK (
        (ambiguity_score IS NULL OR ambiguity_score BETWEEN 0 AND 100)
        AND (completeness_score IS NULL OR completeness_score BETWEEN 0 AND 100)
        AND (quality_score IS NULL OR quality_score BETWEEN 0 AND 100)
    )
);

CREATE INDEX idx_requirement_ai_analysis_status ON requirement_ai_analysis (analysis_status);
CREATE INDEX idx_requirement_ai_analysis_analyzed_at ON requirement_ai_analysis (analyzed_at DESC);
