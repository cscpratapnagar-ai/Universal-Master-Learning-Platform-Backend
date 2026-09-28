CREATE TABLE IF NOT EXISTS billing_invoices (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES billing_orders(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invoice_number VARCHAR(60) NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_billing_invoice_status CHECK (status IN ('ISSUED','PAID','VOID','REFUNDED')),
    CONSTRAINT ck_billing_invoice_amount_non_negative CHECK (amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_billing_invoices_user
    ON billing_invoices(user_id, issued_at DESC);

CREATE TABLE IF NOT EXISTS billing_refunds (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES billing_payments(id) ON DELETE RESTRICT,
    order_id UUID NOT NULL REFERENCES billing_orders(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(40) NOT NULL,
    provider_refund_id VARCHAR(120),
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_billing_refund_status CHECK (status IN ('REQUESTED','PROCESSING','REFUNDED','FAILED')),
    CONSTRAINT ck_billing_refund_amount_positive CHECK (amount > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_billing_refunds_provider
    ON billing_refunds(provider, provider_refund_id)
    WHERE provider_refund_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_billing_refunds_user
    ON billing_refunds(user_id, created_at DESC);
