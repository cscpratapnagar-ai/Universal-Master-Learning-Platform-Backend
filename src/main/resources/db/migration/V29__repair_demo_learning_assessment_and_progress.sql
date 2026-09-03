-- Repair the canonical demo learning flow by business identity.
-- Resolves the real lesson assessment instead of relying on a fixed assessment UUID.
-- Idempotent and safe to run as the pending V29 migration.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Ensure the canonical lesson assessment has at least one question.
WITH target AS (
    SELECT a.id AS assessment_id
    FROM assessments a
    JOIN lessons l ON l.id = a.lesson_id
    WHERE l.id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid
      AND LOWER(a.title) = LOWER('What is Master Learning? - Lesson Assessment')
    ORDER BY a.created_at
    LIMIT 1
), inserted_question AS (
    INSERT INTO assessment_questions (
        id, assessment_id, question_text, question_type, points,
        created_at, updated_at, version
    )
    SELECT
        gen_random_uuid(),
        t.assessment_id,
        'What is the primary goal of Master Learning?',
        'SINGLE_CHOICE',
        10,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    FROM target t
    WHERE NOT EXISTS (
        SELECT 1
        FROM assessment_questions q
        WHERE q.assessment_id = t.assessment_id
    )
    RETURNING id
)
SELECT id FROM inserted_question;

-- Ensure the target assessment has the canonical question, whether it was newly inserted above
-- or was already present from an earlier seed.
WITH target AS (
    SELECT a.id AS assessment_id
    FROM assessments a
    JOIN lessons l ON l.id = a.lesson_id
    WHERE l.id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid
      AND LOWER(a.title) = LOWER('What is Master Learning? - Lesson Assessment')
    ORDER BY a.created_at
    LIMIT 1
), target_question AS (
    SELECT q.id
    FROM assessment_questions q
    JOIN target t ON t.assessment_id = q.assessment_id
    ORDER BY q.created_at
    LIMIT 1
)
INSERT INTO question_options (id, question_id, option_text, correct, created_at, updated_at, version)
SELECT gen_random_uuid(), tq.id, v.option_text, v.correct, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM target_question tq
CROSS JOIN (VALUES
    ('Personalized and measurable learning', TRUE),
    ('Only watching videos', FALSE),
    ('Avoiding assessments', FALSE),
    ('Removing progress tracking', FALSE)
) AS v(option_text, correct)
WHERE NOT EXISTS (
    SELECT 1
    FROM question_options qo
    WHERE qo.question_id = tq.id
      AND qo.option_text = v.option_text
);

-- Reset only the canonical demo student's course progress.
UPDATE course_enrollments ce
SET progress_percent = 0,
    completed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
FROM users u
JOIN courses c ON c.id = ce.course_id
WHERE ce.user_id = u.id
  AND LOWER(u.email) = 'student@masterlearning.local'
  AND (LOWER(COALESCE(c.slug, '')) = 'master-learning-demo-course'
       OR LOWER(c.title) = 'master learning demo course');

-- Reset the demo lesson progress without the invalid cross-reference that broke the first V29.
UPDATE lesson_progress lp
SET completed = FALSE,
    completed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE EXISTS (
    SELECT 1
    FROM course_enrollments ce
    JOIN users u ON u.id = ce.user_id
    JOIN courses c ON c.id = ce.course_id
    WHERE ce.id = lp.enrollment_id
      AND LOWER(u.email) = 'student@masterlearning.local'
      AND (LOWER(COALESCE(c.slug, '')) = 'master-learning-demo-course'
           OR LOWER(c.title) = 'master learning demo course')
)
AND lp.lesson_id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid;

-- Remove old demo attempts so the learner gets a clean three-attempt test window.
DELETE FROM assessment_answers aa
WHERE aa.attempt_id IN (
    SELECT at.id
    FROM assessment_attempts at
    JOIN assessments a ON a.id = at.assessment_id
    JOIN lessons l ON l.id = a.lesson_id
    JOIN users u ON u.id = at.user_id
    WHERE l.id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid
      AND LOWER(a.title) = LOWER('What is Master Learning? - Lesson Assessment')
      AND LOWER(u.email) = 'student@masterlearning.local'
);

DELETE FROM assessment_attempts at
WHERE at.id IN (
    SELECT x.id
    FROM (
        SELECT at2.id
        FROM assessment_attempts at2
        JOIN assessments a ON a.id = at2.assessment_id
        JOIN lessons l ON l.id = a.lesson_id
        JOIN users u ON u.id = at2.user_id
        WHERE l.id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid
          AND LOWER(a.title) = LOWER('What is Master Learning? - Lesson Assessment')
          AND LOWER(u.email) = 'student@masterlearning.local'
    ) x
);
