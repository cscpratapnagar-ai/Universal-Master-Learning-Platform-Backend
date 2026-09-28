-- Disable development/test bootstrap accounts on every environment.
-- The local profile re-enables and repairs these accounts for development use.
UPDATE users
SET enabled = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE LOWER(email) IN (
    'superadmin@masterlearning.local',
    'student@masterlearning.local'
);
