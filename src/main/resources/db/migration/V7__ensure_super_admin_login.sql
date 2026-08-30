-- Ensure the development super administrator can always log in locally.
-- Email: superadmin@masterlearning.local
-- Password: password
-- BCrypt hash generated for Spring Security BCryptPasswordEncoder.

INSERT INTO users (
    id, email, password_hash, first_name, last_name, enabled,
    created_at, updated_at, version
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    'superadmin@masterlearning.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Platform',
    'Super Admin',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    enabled = TRUE,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_roles (user_id, role_id)
SELECT
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001'
WHERE EXISTS (
    SELECT 1 FROM users
    WHERE id = '20000000-0000-0000-0000-000000000001'
)
AND EXISTS (
    SELECT 1 FROM roles
    WHERE id = '10000000-0000-0000-0000-000000000001'
)
ON CONFLICT DO NOTHING;
