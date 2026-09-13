CREATE TABLE assessment_sessions (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    current_question_id UUID REFERENCES assessment_questions(id) ON DELETE SET NULL,
    questions_answered INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX uk_assessment_session_active
    ON assessment_sessions(assessment_id, user_id)
    WHERE status = 'ACTIVE';

CREATE TABLE assessment_session_answers (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES assessment_sessions(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
    selected_option_id UUID REFERENCES question_options(id) ON DELETE SET NULL,
    correct BOOLEAN NOT NULL,
    answered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_session_question UNIQUE(session_id, question_id)
);

CREATE INDEX idx_assessment_session_answers_session ON assessment_session_answers(session_id);
