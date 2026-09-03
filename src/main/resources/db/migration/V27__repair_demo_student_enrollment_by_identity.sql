-- Repair the demo learner enrollment using stable business identity rather than
-- relying only on seeded UUID values. This is idempotent and preserves progress.
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
    gen_random_uuid(),
    c.id,
    u.id,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM users u
CROSS JOIN LATERAL (
    SELECT id
    FROM courses
    WHERE LOWER(COALESCE(slug, '')) = 'master-learning-demo-course'
       OR LOWER(title) = 'master learning demo course'
    ORDER BY CASE
        WHEN LOWER(COALESCE(slug, '')) = 'master-learning-demo-course' THEN 0
        ELSE 1
    END, created_at
    LIMIT 1
) c
WHERE LOWER(u.email) = 'student@masterlearning.local'
ON CONFLICT (course_id, user_id) DO NOTHING;
