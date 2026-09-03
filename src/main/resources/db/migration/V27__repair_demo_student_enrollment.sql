-- Repair the demo learner enrollment for databases where V26 already ran
-- before the demo student was present. Idempotent and preserves progress.
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
    md5(lower(u.email) || ':' || c.id::text)::uuid,
    c.id,
    u.id,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM users u
JOIN courses c
  ON c.id = 'e55f03cf-b3ca-4910-bb7b-18abdc836da3'::uuid
WHERE lower(u.email) = 'student@masterlearning.local'
ON CONFLICT (course_id, user_id) DO NOTHING;
