-- Seed the canonical demo lesson assessment with one real question.
-- Idempotent: never duplicates an existing question or options.

INSERT INTO assessment_questions (
    id,
    assessment_id,
    question_text,
    question_type,
    points,
    created_at,
    updated_at,
    version
)
SELECT
    '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid,
    'ce8432ea-a5d2-4841-b88d-b84744c1a8a6'::uuid,
    'What is the primary goal of Master Learning?',
    'SINGLE_CHOICE',
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE EXISTS (
    SELECT 1 FROM assessments
    WHERE id = 'ce8432ea-a5d2-4841-b88d-b84744c1a8a6'::uuid
)
AND NOT EXISTS (
    SELECT 1 FROM assessment_questions
    WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid
);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000001'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid, 'Personalized and measurable learning', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000001'::uuid);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000002'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid, 'Only watching videos', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000002'::uuid);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000003'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid, 'Avoiding assessments', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000003'::uuid);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000004'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid, 'Removing progress tracking', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000004'::uuid);
