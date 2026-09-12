-- Concept/skill taxonomy for learner intelligence.
-- Concepts are course-scoped and may form a parent-child hierarchy.

CREATE TABLE IF NOT EXISTS learning_concepts (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    parent_id UUID,
    code VARCHAR(120) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    concept_type VARCHAR(20) NOT NULL DEFAULT 'CONCEPT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_learning_concept_course_code UNIQUE (course_id, code),
    CONSTRAINT ck_learning_concept_type CHECK (concept_type IN ('CONCEPT', 'SKILL')),
    CONSTRAINT fk_learning_concept_parent FOREIGN KEY (parent_id) REFERENCES learning_concepts(id)
);

CREATE INDEX IF NOT EXISTS idx_learning_concepts_course
    ON learning_concepts(course_id);

CREATE INDEX IF NOT EXISTS idx_learning_concepts_parent
    ON learning_concepts(parent_id);

CREATE TABLE IF NOT EXISTS lesson_concepts (
    lesson_id UUID NOT NULL,
    concept_id UUID NOT NULL,
    PRIMARY KEY (lesson_id, concept_id),
    CONSTRAINT fk_lesson_concept_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_concept_concept FOREIGN KEY (concept_id) REFERENCES learning_concepts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lesson_concepts_concept
    ON lesson_concepts(concept_id);

CREATE TABLE IF NOT EXISTS question_concepts (
    question_id UUID NOT NULL,
    concept_id UUID NOT NULL,
    PRIMARY KEY (question_id, concept_id),
    CONSTRAINT fk_question_concept_question FOREIGN KEY (question_id) REFERENCES assessment_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_question_concept_concept FOREIGN KEY (concept_id) REFERENCES learning_concepts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_question_concepts_concept
    ON question_concepts(concept_id);
