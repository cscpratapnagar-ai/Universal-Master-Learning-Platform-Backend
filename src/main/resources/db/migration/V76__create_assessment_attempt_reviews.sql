CREATE TABLE IF NOT EXISTS assessment_attempt_reviews (
    id UUID PRIMARY KEY,
    attempt_id UUID NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES users(id),
    remarks TEXT NOT NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_assessment_attempt_review_attempt UNIQUE (attempt_id)
);
CREATE INDEX IF NOT EXISTS idx_assessment_attempt_reviews_reviewer
    ON assessment_attempt_reviews (reviewer_id);