INSERT INTO permissions (
    id, code, name, description, created_at, updated_at, version
) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SYSTEM:READ', 'Read System', 'View system-level information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000002', 'USER:READ', 'Read Users', 'View users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000003', 'USER:CREATE', 'Create Users', 'Create users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000004', 'USER:UPDATE', 'Update Users', 'Update users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000005', 'USER:DELETE', 'Delete Users', 'Delete users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO roles (
    id, code, name, description, system_role, created_at, updated_at, version
) VALUES
    ('10000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Super Administrator', 'Full platform control', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('10000000-0000-0000-0000-000000000002', 'ADMIN', 'Administrator', 'Administrative access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('10000000-0000-0000-0000-000000000003', 'STUDENT', 'Student', 'Student access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('10000000-0000-0000-0000-000000000004', 'TEACHER', 'Teacher', 'Teacher access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000001',
    id
FROM permissions;
