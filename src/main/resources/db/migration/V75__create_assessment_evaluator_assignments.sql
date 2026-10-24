CREATE TABLE IF NOT EXISTS assessment_evaluator_assignments (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    evaluator_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_assessment_evaluator_assignment UNIQUE (assessment_id, evaluator_id)
);
CREATE INDEX IF NOT EXISTS idx_evaluator_assignment_evaluator
    ON assessment_evaluator_assignments (evaluator_id);
CREATE INDEX IF NOT EXISTS idx_evaluator_assignment_assessment
    ON assessment_evaluator_assignments (assessment_id);
