ALTER TABLE assessment_attempts
    ADD COLUMN IF NOT EXISTS attempt_number INTEGER;

UPDATE assessment_attempts
SET attempt_number = 1
WHERE attempt_number IS NULL;

ALTER TABLE assessment_attempts
    ALTER COLUMN attempt_number SET NOT NULL;

ALTER TABLE assessment_attempts
    ADD COLUMN IF NOT EXISTS mastery_level VARCHAR(30);

UPDATE assessment_attempts
SET mastery_level = 'BEGINNER'
WHERE mastery_level IS NULL;

ALTER TABLE assessment_attempts
    ALTER COLUMN mastery_level SET NOT NULL;

ALTER TABLE assessment_attempts
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP WITH TIME ZONE;

UPDATE assessment_attempts
SET submitted_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
WHERE submitted_at IS NULL;

ALTER TABLE assessment_attempts
    ALTER COLUMN submitted_at SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_assessment_attempt_number'
    ) THEN
        ALTER TABLE assessment_attempts
            ADD CONSTRAINT uk_assessment_attempt_number
            UNIQUE (assessment_id, user_id, attempt_number);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_assessment_attempts_assessment_user
    ON assessment_attempts(assessment_id, user_id);
