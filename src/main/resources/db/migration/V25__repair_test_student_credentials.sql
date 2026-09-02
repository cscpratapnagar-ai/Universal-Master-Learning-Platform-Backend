-- Repair the development test student credentials for databases that already applied V24.
-- Development-only account; remove or rotate before production deployment.

UPDATE users
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    enabled = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'student@masterlearning.local';

INSERT INTO user_roles (user_id, role_id)
SELECT
    u.id,
    '10000000-0000-0000-0000-000000000003'
FROM users u
WHERE u.email = 'student@masterlearning.local'
  AND EXISTS (
      SELECT 1
      FROM roles r
      WHERE r.id = '10000000-0000-0000-0000-000000000003'
  )
ON CONFLICT DO NOTHING;
