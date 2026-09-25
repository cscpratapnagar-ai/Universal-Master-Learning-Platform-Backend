-- Repair organization administrator workspace membership deterministically.
-- If there is exactly one active organization, every enabled ORG_ADMIN without
-- an active membership is attached to that tenant. This is intentionally
-- constrained to the single-tenant case to avoid cross-tenant guessing.

UPDATE organization_members om
SET active = TRUE,
    updated_at = CURRENT_TIMESTAMP
FROM organizations o
JOIN user_roles ur ON ur.role_id = (
    SELECT r.id FROM roles r WHERE r.code = 'ORG_ADMIN' LIMIT 1
)
JOIN users u ON u.id = ur.user_id
WHERE om.organization_id = o.id
  AND om.user_id = u.id
  AND o.active = TRUE
  AND u.enabled = TRUE
  AND (SELECT COUNT(*) FROM organizations WHERE active = TRUE) = 1;

INSERT INTO organization_members (
    id, organization_id, user_id, active, created_at, updated_at, version
)
SELECT
    gen_random_uuid(),
    o.id,
    u.id,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM organizations o
CROSS JOIN users u
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r ON r.id = ur.role_id AND r.code = 'ORG_ADMIN'
WHERE o.active = TRUE
  AND u.enabled = TRUE
  AND (SELECT COUNT(*) FROM organizations WHERE active = TRUE) = 1
  AND NOT EXISTS (
      SELECT 1
      FROM organization_members existing
      WHERE existing.organization_id = o.id
        AND existing.user_id = u.id
  );