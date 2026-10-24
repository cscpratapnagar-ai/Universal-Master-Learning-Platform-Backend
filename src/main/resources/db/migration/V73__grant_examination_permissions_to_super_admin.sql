-- Permissions added after the original seed migration must also be granted to SUPER_ADMIN.
-- Keep the operation repeatable for environments that already contain some mappings.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('ASSESSMENT:CREATE', 'ASSESSMENT:REVIEW', 'EXAM:OVERSIGHT', 'EXAM:PROCTOR')
WHERE r.code = 'SUPER_ADMIN'
AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
