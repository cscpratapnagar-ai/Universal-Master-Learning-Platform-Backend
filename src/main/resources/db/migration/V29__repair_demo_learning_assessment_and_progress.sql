-- Compatibility migration kept intentionally no-op.
-- The canonical demo enrollment and assessment question are already repaired by
-- V27 and V28. Keeping this migration side-effect free guarantees that older
-- local databases can advance through Flyway without depending on demo data
-- shape or PostgreSQL UPDATE/subquery behavior.

SELECT 1;
