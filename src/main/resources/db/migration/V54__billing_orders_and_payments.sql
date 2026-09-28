CREATE TABLE IF NOT EXISTS billing_orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    billing_cycle VARCHAR(20) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL,
    external_order_id VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_billing_order_cycle CHECK (billing_cycle IN ('MONTHLY','YEARLY')),
    CONSTRAINT ck_billing_order_status CHECK (status IN ('CREATED','PENDING_PAYMENT','PAID','FAILED','CANCELLED','REFUNDED')),
    CONSTRAINT ck_billing_order_amount_non_negative CHECK (amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_billing_orders_user_created
    ON billing_orders(user_id, created_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uk_billing_orders_external
    ON billing_orders(external_order_id)
    WHERE external_order_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS billing_payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES billing_orders(id) ON DELETE CASCADE,
    provider VARCHAR(40) NOT NULL,
    provider_payment_id VARCHAR(120),
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(500),
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_billing_payment_status CHECK (status IN ('CREATED','AUTHORIZED','CAPTURED','FAILED','REFUNDED')),
    CONSTRAINT ck_billing_payment_amount_non_negative CHECK (amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_billing_payments_order
    ON billing_payments(order_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_billing_payments_provider
    ON billing_payments(provider, provider_payment_id)
    WHERE provider_payment_id IS NOT NULL;
