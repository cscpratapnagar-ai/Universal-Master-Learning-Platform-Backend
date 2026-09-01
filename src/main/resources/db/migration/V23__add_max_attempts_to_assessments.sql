-- Align assessments table with Assessment entity.
-- Safe for existing databases and fresh installations.

ALTER TABLE assessments
    ADD COLUMN IF NOT EXISTS max_attempts INTEGER;

UPDATE assessments
SET max_attempts = 3
WHERE max_attempts IS NULL;

ALTER TABLE assessments
    ALTER COLUMN max_attempts SET DEFAULT 3;

ALTER TABLE assessments
    ALTER COLUMN max_attempts SET NOT NULL;
