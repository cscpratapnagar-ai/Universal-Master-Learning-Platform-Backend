-- Development-only test student account.
-- Password: password
-- IMPORTANT: remove or rotate before production deployment.

INSERT INTO users (
    id, email, password_hash, first_name, last_name, enabled,
    created_at, updated_at, version
) VALUES (
    '30000000-0000-0000-0000-000000000001',
    'student@masterlearning.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Test',
    'Student',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT
    '30000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000003'
WHERE EXISTS (
    SELECT 1 FROM users WHERE id = '30000000-0000-0000-0000-000000000001'
)
AND EXISTS (
    SELECT 1 FROM roles WHERE id = '10000000-0000-0000-0000-000000000003'
)
ON CONFLICT DO NOTHING;
