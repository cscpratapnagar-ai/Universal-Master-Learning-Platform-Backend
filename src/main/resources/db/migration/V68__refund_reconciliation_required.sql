-- Unknown gateway outcomes must remain reserved until manually reconciled.
ALTER TABLE billing_refunds DROP CONSTRAINT IF EXISTS ck_billing_refund_status;
ALTER TABLE billing_refunds ADD CONSTRAINT ck_billing_refund_status
    CHECK (status IN ('REQUESTED','PROCESSING','REFUNDED','FAILED','RECONCILIATION_REQUIRED'));
