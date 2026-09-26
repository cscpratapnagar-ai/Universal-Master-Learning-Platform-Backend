CREATE TABLE IF NOT EXISTS program_activity (
 id UUID PRIMARY KEY,
 program_id UUID NOT NULL REFERENCES programs(id),
 action VARCHAR(60) NOT NULL,
 details VARCHAR(2000),
 actor VARCHAR(180) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_program_activity_program_created ON program_activity(program_id, created_at DESC);