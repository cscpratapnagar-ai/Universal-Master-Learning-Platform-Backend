-- Keep existing assessment-authoring roles aligned with the explicit permission model.
-- This is additive and idempotent; endpoint-level permission enforcement is a separate step.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'ASSESSMENT:CREATE'
WHERE r.code IN ('SUPER_ADMIN', 'ADMIN', 'INSTRUCTOR', 'TEACHER')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
