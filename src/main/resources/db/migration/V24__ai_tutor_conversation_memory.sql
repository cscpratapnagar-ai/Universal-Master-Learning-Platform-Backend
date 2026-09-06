-- Persistent AI Tutor conversation memory scoped to a learner enrollment.
CREATE TABLE IF NOT EXISTS ai_tutor_conversation_turns (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ai_tutor_turn_role CHECK (role IN ('LEARNER', 'TUTOR'))
);

CREATE INDEX IF NOT EXISTS idx_ai_tutor_turns_enrollment_created
    ON ai_tutor_conversation_turns(enrollment_id, created_at DESC);
