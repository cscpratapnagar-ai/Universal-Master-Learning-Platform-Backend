CREATE TABLE IF NOT EXISTS billing_webhook_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(40) NOT NULL,
    event_id VARCHAR(180) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_hash VARCHAR(128) NOT NULL,
    status VARCHAR(30) NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    error_message VARCHAR(1000),
    CONSTRAINT ck_billing_webhook_status CHECK (status IN ('RECEIVED','PROCESSED','FAILED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_billing_webhook_provider_event
    ON billing_webhook_events(provider, event_id);

CREATE INDEX IF NOT EXISTS idx_billing_webhook_received
    ON billing_webhook_events(received_at DESC);
