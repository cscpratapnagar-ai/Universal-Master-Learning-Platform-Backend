CREATE TABLE learning_activities (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES course_enrollments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(30) NOT NULL,
    duration_seconds INT NOT NULL DEFAULT 0 CHECK (duration_seconds BETWEEN 0 AND 3600),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_learning_activity_enrollment_time ON learning_activities(enrollment_id, occurred_at DESC);
CREATE INDEX idx_learning_activity_user_time ON learning_activities(user_id, occurred_at DESC);
CREATE INDEX idx_learning_activity_lesson ON learning_activities(lesson_id);
