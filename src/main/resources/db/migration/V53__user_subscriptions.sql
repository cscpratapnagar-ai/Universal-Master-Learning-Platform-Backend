CREATE TABLE IF NOT EXISTS user_subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(30) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    external_customer_id VARCHAR(120),
    external_subscription_id VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_subscription_status CHECK (status IN ('PENDING','ACTIVE','PAST_DUE','CANCELLED','EXPIRED')),
    CONSTRAINT ck_user_subscription_cycle CHECK (billing_cycle IN ('MONTHLY','YEARLY')),
    CONSTRAINT ck_user_subscription_period CHECK (current_period_end >= current_period_start)
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user_status
    ON user_subscriptions(user_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_subscriptions_external
    ON user_subscriptions(external_subscription_id)
    WHERE external_subscription_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_active_subscription
    ON user_subscriptions(user_id)
    WHERE status IN ('PENDING','ACTIVE','PAST_DUE');
