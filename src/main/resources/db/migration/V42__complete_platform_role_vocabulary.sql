-- Complete the platform role vocabulary used by the frontend and registration flow.
INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000005', 'LEARNER', 'Learner', 'Learner access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'LEARNER');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000006', 'INSTRUCTOR', 'Instructor', 'Instructor access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'INSTRUCTOR');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000007', 'ORG_ADMIN', 'Organization Administrator', 'Organization administration access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'ORG_ADMIN');
