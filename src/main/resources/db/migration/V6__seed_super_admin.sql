-- Development-only bootstrap account for the internal platform portal.
-- Password: ChangeMe123! (BCrypt hash). Rotate/remove this account for production.

INSERT INTO users (
    id, email, password_hash, first_name, last_name, enabled,
    created_at, updated_at, version
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    'superadmin@masterlearning.local',
    '$2a$10$zK1VjK9k1WkEMZPqYQe6KeY9Jz5d8w6hLQwRr1Pp6sP5R9mX7w6lO',
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