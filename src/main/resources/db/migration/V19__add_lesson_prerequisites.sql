CREATE TABLE IF NOT EXISTS lesson_prerequisites (
    lesson_id UUID NOT NULL,
    prerequisite_lesson_id UUID NOT NULL,
    PRIMARY KEY (lesson_id, prerequisite_lesson_id),
    CONSTRAINT fk_lesson_prerequisite_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_prerequisite_required
        FOREIGN KEY (prerequisite_lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT chk_lesson_prerequisite_not_self
        CHECK (lesson_id <> prerequisite_lesson_id)
);