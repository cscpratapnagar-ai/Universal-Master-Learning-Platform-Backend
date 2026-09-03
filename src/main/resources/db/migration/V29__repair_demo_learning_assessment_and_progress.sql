-- Repair the canonical demo learner progress state.
-- This migration intentionally contains only simple, non-correlated UPDATEs.
-- Assessment question seeding is handled by the following V30 migration.

UPDATE course_enrollments
SET progress_percent = 0,
    completed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id IN (
    SELECT users.id
    FROM users
    WHERE LOWER(users.email) = 'student@masterlearning.local'
)
AND course_id IN (
    SELECT courses.id
    FROM courses
    WHERE LOWER(COALESCE(courses.slug, '')) = 'master-learning-demo-course'
       OR LOWER(courses.title) = 'master learning demo course'
);

UPDATE lesson_progress
SET completed = FALSE,
    completed_at = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE enrollment_id IN (
    SELECT course_enrollments.id
    FROM course_enrollments
    JOIN users ON users.id = course_enrollments.user_id
    JOIN courses ON courses.id = course_enrollments.course_id
    WHERE LOWER(users.email) = 'student@masterlearning.local'
      AND (
          LOWER(COALESCE(courses.slug, '')) = 'master-learning-demo-course'
          OR LOWER(courses.title) = 'master learning demo course'
      )
)
AND lesson_id = '5860779a-3f89-4a40-aad7-7251925c05df'::uuid;
