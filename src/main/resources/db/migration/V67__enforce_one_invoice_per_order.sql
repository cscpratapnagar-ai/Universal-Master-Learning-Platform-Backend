-- A billing order must never have more than one invoice, even if application-level
-- idempotency checks race. Fail closed with an actionable message if legacy data
-- already violates the invariant; do not silently delete financial records.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM billing_invoices
        GROUP BY order_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot enforce one invoice per billing order: duplicate billing_invoices.order_id values exist. Reconcile duplicates before retrying migration V67.';
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_billing_invoices_order
    ON billing_invoices(order_id);
