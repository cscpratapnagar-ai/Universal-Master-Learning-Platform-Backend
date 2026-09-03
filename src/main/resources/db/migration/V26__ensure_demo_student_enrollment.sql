-- Ensure the canonical local demo student is enrolled in the canonical demo course.
-- This is intentionally idempotent and does not overwrite existing progress.
INSERT INTO course_enrollments (
    id,
    course_id,
    user_id,
    progress_percent,
    created_at,
    updated_at,
    version
)
SELECT
    '719ca5af-c2fa-45f8-8b0a-216dd05fe5b0'::uuid,
    'e55f03cf-b3ca-4910-bb7b-18abdc836da3'::uuid,
    u.id,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM users u
JOIN courses c ON c.id = 'e55f03cf-b3ca-4910-bb7b-18abdc836da3'::uuid
WHERE u.id = '30000000-0000-0000-0000-000000000001'::uuid
   OR LOWER(u.email) = 'student@masterlearning.local'
ON CONFLICT (course_id, user_id) DO NOTHING;
