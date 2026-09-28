CREATE TABLE IF NOT EXISTS ai_teacher_usage (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    turn_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ai_teacher_usage_user_period UNIQUE (user_id, period_start),
    CONSTRAINT ck_ai_teacher_usage_turn_count_non_negative CHECK (turn_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_ai_teacher_usage_user_period
    ON ai_teacher_usage(user_id, period_start);
