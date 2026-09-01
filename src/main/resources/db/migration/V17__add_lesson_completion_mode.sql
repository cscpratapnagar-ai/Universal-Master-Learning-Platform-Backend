ALTER TABLE lessons
    ADD COLUMN IF NOT EXISTS completion_mode VARCHAR(30) NOT NULL DEFAULT 'MANUAL_COMPLETE';

ALTER TABLE lessons
    DROP CONSTRAINT IF EXISTS chk_lessons_completion_mode;

ALTER TABLE lessons
    ADD CONSTRAINT chk_lessons_completion_mode
    CHECK (completion_mode IN ('AUTO_COMPLETE', 'MANUAL_COMPLETE', 'ASSESSMENT_REQUIRED'));
