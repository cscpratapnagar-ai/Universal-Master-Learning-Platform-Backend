-- A refund marked REFUNDED must be traceable to the gateway's refund identifier.
-- Fail with a clear remediation message rather than silently altering financial history.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM billing_refunds
        WHERE status = 'REFUNDED'
          AND (provider_refund_id IS NULL OR BTRIM(provider_refund_id) = '')
    ) THEN
        RAISE EXCEPTION 'Cannot enforce confirmed refund identifiers: billing_refunds contains REFUNDED rows without provider_refund_id. Reconcile these financial records before retrying migration V69.';
    END IF;
END $$;

ALTER TABLE billing_refunds
    ADD CONSTRAINT ck_billing_refund_confirmed_has_provider_id
    CHECK (status <> 'REFUNDED' OR (provider_refund_id IS NOT NULL AND BTRIM(provider_refund_id) <> ''));
