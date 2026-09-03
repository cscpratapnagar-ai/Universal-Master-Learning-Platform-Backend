-- Repair the canonical demo learning flow by business identity.
-- Idempotent and safe for the local demo learner.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO assessment_questions (
    id, assessment_id, question_text, question_type, points,
    created_at, updated_at, version
)
SELECT
    '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid,
    a.id,
    'What is the primary goal of Master Learning?',
    'SINGLE_CHOICE',
    10,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM assessments a
JOIN lessons l ON l.id = a.lesson_id
WHERE l.id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid
  AND LOWER(a.title) = LOWER('What is Master Learning? - Lesson Assessment')
  AND NOT EXISTS (
      SELECT 1 FROM assessment_questions q
      WHERE q.id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid
  )
LIMIT 1;

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000001'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid,
       'Personalized and measurable learning', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
  AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000001'::uuid);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000002'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid,
       'Only watching videos', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
  AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000002'::uuid);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000003'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid,
       'Avoiding assessments', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
  AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000003'::uuid);

INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT 'a1000000-0000-0000-0000-000000000004'::uuid, '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid,
       'Removing progress tracking', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE EXISTS (SELECT 1 FROM assessment_questions WHERE id = '70957314-6d8e-42e3-9629-9c7f986083a4'::uuid)
  AND NOT EXISTS (SELECT 1 FROM question_options WHERE id = 'a1000000-0000-0000-0000-000000000004'::uuid);

UPDATE course_enrollments ce
SET progress_percent = 0,
    completed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE ce.id IN (
    SELECT ce2.id
    FROM course_enrollments ce2
    JOIN users u ON u.id = ce2.user_id
    JOIN courses c ON c.id = ce2.course_id
    WHERE LOWER(u.email) = 'student@masterlearning.local'
      AND (LOWER(COALESCE(c.slug, '')) = 'master-learning-demo-course'
           OR LOWER(c.title) = 'master learning demo course')
);

UPDATE lesson_progress lp
SET completed = FALSE,
    completed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE lp.enrollment_id IN (
    SELECT ce.id
    FROM course_enrollments ce
    JOIN users u ON u.id = ce.user_id
    JOIN courses c ON c.id = ce.course_id
    WHERE LOWER(u.email) = 'student@masterlearning.local'
      AND (LOWER(COALESCE(c.slug, '')) = 'master-learning-demo-course'
           OR LOWER(c.title) = 'master learning demo course')
)
AND lp.lesson_id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid;
