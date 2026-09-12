ALTER TABLE assessment_questions
    ADD COLUMN IF NOT EXISTS difficulty_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

ALTER TABLE assessment_questions
    ADD CONSTRAINT assessment_questions_difficulty_level_chk
    CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD'));

CREATE INDEX IF NOT EXISTS idx_assessment_questions_difficulty
    ON assessment_questions (assessment_id, difficulty_level);
