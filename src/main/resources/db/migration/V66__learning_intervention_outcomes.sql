CREATE TABLE learning_intervention_outcomes (
 id UUID PRIMARY KEY, enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, intervention_type VARCHAR(60) NOT NULL,
 target_lesson_id UUID, outcome VARCHAR(40) NOT NULL, mastery_delta DOUBLE PRECISION, notes TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_learning_intervention_mastery_delta CHECK (mastery_delta IS NULL OR (mastery_delta >= -100 AND mastery_delta <= 100))
);
CREATE INDEX idx_learning_intervention_outcomes_enrollment_created ON learning_intervention_outcomes(enrollment_id,created_at DESC);
CREATE INDEX idx_learning_intervention_outcomes_user_created ON learning_intervention_outcomes(user_id,created_at DESC);