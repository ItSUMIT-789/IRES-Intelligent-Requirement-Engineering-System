ALTER TABLE requirement_traceability_links
    DROP CONSTRAINT ck_traceability_source_type,
    DROP CONSTRAINT ck_traceability_target_type;

ALTER TABLE requirement_traceability_links
    ADD CONSTRAINT ck_traceability_source_type CHECK (source_type IN ('REQUIREMENT', 'USER_STORY', 'ACCEPTANCE_CRITERIA', 'DEVELOPER_TASK', 'TEST_CASE', 'TEST_EXECUTION', 'BUG')),
    ADD CONSTRAINT ck_traceability_target_type CHECK (target_type IN ('REQUIREMENT', 'USER_STORY', 'ACCEPTANCE_CRITERIA', 'DEVELOPER_TASK', 'TEST_CASE', 'TEST_EXECUTION', 'BUG'));
