ALTER TABLE assessments
    ADD COLUMN IF NOT EXISTS module_id UUID,
    ADD COLUMN IF NOT EXISTS lesson_id UUID,
    ADD COLUMN IF NOT EXISTS assessment_level VARCHAR(30) NOT NULL DEFAULT 'COURSE';

ALTER TABLE assessments
    DROP CONSTRAINT IF EXISTS chk_assessments_level;

ALTER TABLE assessments
    ADD CONSTRAINT chk_assessments_level
    CHECK (assessment_level IN ('COURSE', 'MODULE', 'LESSON'));

ALTER TABLE assessments
    DROP CONSTRAINT IF EXISTS fk_assessments_module;

ALTER TABLE assessments
    ADD CONSTRAINT fk_assessments_module
    FOREIGN KEY (module_id) REFERENCES course_modules(id) ON DELETE CASCADE;

ALTER TABLE assessments
    DROP CONSTRAINT IF EXISTS fk_assessments_lesson;

ALTER TABLE assessments
    ADD CONSTRAINT fk_assessments_lesson
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;
