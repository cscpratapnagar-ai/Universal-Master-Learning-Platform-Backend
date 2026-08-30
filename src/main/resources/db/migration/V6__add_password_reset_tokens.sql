CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX idx_password_reset_token_hash ON password_reset_tokens(token_hash);
