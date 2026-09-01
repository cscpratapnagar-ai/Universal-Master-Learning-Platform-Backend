CREATE TABLE IF NOT EXISTS assessments (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id),
    module_id UUID REFERENCES course_modules(id),
    lesson_id UUID REFERENCES lessons(id),
    assessment_level VARCHAR(30) NOT NULL DEFAULT 'COURSE',
    title VARCHAR(180) NOT NULL,
    passing_score INTEGER NOT NULL DEFAULT 60,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS assessment_attempts (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessments(id),
    user_id UUID NOT NULL REFERENCES users(id),
    attempt_number INTEGER NOT NULL,
    score INTEGER NOT NULL,
    passed BOOLEAN NOT NULL,
    mastery_level VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_assessment_attempt_number UNIQUE (assessment_id, user_id, attempt_number)
);

CREATE TABLE IF NOT EXISTS assessment_answers (
    id UUID PRIMARY KEY,
    attempt_id UUID NOT NULL REFERENCES assessment_attempts(id),
    question_id UUID NOT NULL REFERENCES assessment_questions(id),
    selected_option_id UUID REFERENCES question_options(id),
    correct BOOLEAN NOT NULL,
    points_awarded INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_assessment_attempts_assessment_user
    ON assessment_attempts(assessment_id, user_id);

CREATE INDEX IF NOT EXISTS idx_assessment_answers_attempt
    ON assessment_answers(attempt_id);
