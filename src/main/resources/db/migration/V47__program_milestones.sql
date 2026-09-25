CREATE TABLE IF NOT EXISTS program_milestones (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES programs(id),
    title VARCHAR(180) NOT NULL,
    description VARCHAR(2500),
    due_date DATE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_program_milestones_program_order ON program_milestones(program_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_program_milestones_program_status ON program_milestones(program_id, status);
