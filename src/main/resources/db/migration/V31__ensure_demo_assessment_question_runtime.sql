-- Ensure the canonical demo lesson assessment has at least one question.
-- V28 may have executed before the assessment existed in an older local database,
-- so this migration repairs that state without depending on a specific assessment UUID.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    assessment_id UUID;
    question_id UUID;
BEGIN
    SELECT a.id
      INTO assessment_id
    FROM assessments a
    JOIN lessons l ON l.id = a.lesson_id
    JOIN course_modules cm ON cm.id = l.module_id
    JOIN courses c ON c.id = cm.course_id
    WHERE LOWER(l.title) = 'what is master learning?'
      AND LOWER(c.title) = 'master learning demo course'
      AND NOT EXISTS (
          SELECT 1
          FROM assessment_questions q
          WHERE q.assessment_id = a.id
      )
    ORDER BY a.created_at
    LIMIT 1;

    IF assessment_id IS NOT NULL THEN
        question_id := gen_random_uuid();

        INSERT INTO assessment_questions (
            id, assessment_id, question_text, question_type, points,
            created_at, updated_at, version
        ) VALUES (
            question_id,
            assessment_id,
            'What is the primary goal of Master Learning?',
            'SINGLE_CHOICE',
            10,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP,
            0
        );

        INSERT INTO question_options (
            id, question_id, option_text, correct,
            created_at, updated_at, version
        ) VALUES
        (gen_random_uuid(), question_id, 'Personalized and measurable learning', TRUE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
        (gen_random_uuid(), question_id, 'Only watching videos', FALSE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
        (gen_random_uuid(), question_id, 'Avoiding assessments', FALSE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
        (gen_random_uuid(), question_id, 'Removing progress tracking', FALSE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
    END IF;
END $$;
