-- Final idempotent repair for the canonical demo assessment.
-- If an assessment is attached to the canonical lesson but has no questions,
-- seed one question directly against that assessment. This deliberately does
-- not depend on an assessment UUID or title because those can vary between
-- environments.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    a RECORD;
    q_id UUID;
BEGIN
    FOR a IN
        SELECT a.id
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
    LOOP
        q_id := gen_random_uuid();

        INSERT INTO assessment_questions (
            id, assessment_id, question_text, question_type, points,
            created_at, updated_at, version
        ) VALUES (
            q_id,
            a.id,
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
        (gen_random_uuid(), q_id, 'Personalized and measurable learning', TRUE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
        (gen_random_uuid(), q_id, 'Only watching videos', FALSE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
        (gen_random_uuid(), q_id, 'Avoiding assessments', FALSE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
        (gen_random_uuid(), q_id, 'Removing progress tracking', FALSE,
         CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
    END LOOP;
END $$;
