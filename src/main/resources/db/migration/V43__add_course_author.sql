ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS created_by UUID;

ALTER TABLE courses
    DROP CONSTRAINT IF EXISTS fk_courses_created_by;

ALTER TABLE courses
    ADD CONSTRAINT fk_courses_created_by
    FOREIGN KEY (created_by) REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_courses_created_by ON courses(created_by);
