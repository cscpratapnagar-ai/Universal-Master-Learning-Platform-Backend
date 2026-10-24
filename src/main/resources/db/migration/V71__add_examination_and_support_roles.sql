-- Add role vocabulary for examination and support workflows without modifying applied migrations.
INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000008', 'EXAMINER', 'Examiner', 'Assigned examination oversight and review', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'EXAMINER');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000009', 'QUESTION_SETTER', 'Question Paper Setter', 'Create and maintain examination questions', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'QUESTION_SETTER');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000010', 'EVALUATOR', 'Answer Evaluator', 'Evaluate submitted examination answers', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'EVALUATOR');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000011', 'PROCTOR', 'Exam Invigilator / Proctor', 'Monitor examination sessions', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'PROCTOR');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000012', 'PARENT', 'Parent / Guardian', 'View linked learner progress', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'PARENT');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000013', 'FINANCE', 'Finance / Accounts', 'Manage permitted billing and finance workflows', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'FINANCE');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000014', 'SUPPORT', 'Support / Helpdesk', 'Assist users with support requests', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'SUPPORT');

INSERT INTO roles (id, code, name, description, system_role, created_at, updated_at, version)
SELECT '10000000-0000-0000-0000-000000000015', 'PRIVATE_TEACHER', 'Private Teacher / Tutor', 'Private tutoring and booking access', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'PRIVATE_TEACHER');
