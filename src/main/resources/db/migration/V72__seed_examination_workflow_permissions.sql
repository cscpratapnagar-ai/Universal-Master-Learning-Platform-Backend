-- Seed least-privilege permission vocabulary for examination workflows.
-- Endpoint enforcement is added separately; this migration does not grant access by itself.
INSERT INTO permissions (id, code, name, description, created_at, updated_at, version)
SELECT '00000000-0000-0000-0000-000000000021', 'ASSESSMENT:CREATE', 'Create Assessments', 'Create and maintain assessment definitions and questions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ASSESSMENT:CREATE');

INSERT INTO permissions (id, code, name, description, created_at, updated_at, version)
SELECT '00000000-0000-0000-0000-000000000022', 'ASSESSMENT:REVIEW', 'Review Assessment Attempts', 'Review submitted assessment attempts and evaluation evidence', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ASSESSMENT:REVIEW');

INSERT INTO permissions (id, code, name, description, created_at, updated_at, version)
SELECT '00000000-0000-0000-0000-000000000023', 'EXAM:OVERSIGHT', 'Examination Oversight', 'View examination oversight and governance information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'EXAM:OVERSIGHT');

INSERT INTO permissions (id, code, name, description, created_at, updated_at, version)
SELECT '00000000-0000-0000-0000-000000000024', 'EXAM:PROCTOR', 'Examination Proctoring', 'Access authorized examination monitoring operations', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'EXAM:PROCTOR');

-- Question setters receive authoring only; evaluation, oversight and proctoring remain separate.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'QUESTION_SETTER' AND p.code = 'ASSESSMENT:CREATE'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'EVALUATOR' AND p.code = 'ASSESSMENT:REVIEW'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'EXAMINER' AND p.code = 'EXAM:OVERSIGHT'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'PROCTOR' AND p.code = 'EXAM:PROCTOR'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);
