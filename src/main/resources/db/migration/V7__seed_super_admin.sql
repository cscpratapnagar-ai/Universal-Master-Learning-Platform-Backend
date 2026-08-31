-- Development-only bootstrap account for the internal platform portal.
-- Password: password
-- IMPORTANT: rotate/remove this bootstrap credential before production deployment.

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
);

INSERT INTO user_roles (user_id, role_id) VALUES (
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001'
);