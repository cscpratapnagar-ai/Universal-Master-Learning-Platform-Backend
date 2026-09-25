CREATE TABLE IF NOT EXISTS program_dependencies (
 id UUID PRIMARY KEY,
 program_id UUID NOT NULL REFERENCES programs(id),
 predecessor_id UUID NOT NULL REFERENCES program_milestones(id),
 successor_id UUID NOT NULL REFERENCES program_milestones(id),
 type VARCHAR(20) NOT NULL DEFAULT 'BLOCKS',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT uk_program_dependency_edge UNIQUE(predecessor_id, successor_id),
 CONSTRAINT chk_program_dependency_not_self CHECK(predecessor_id <> successor_id)
);
CREATE INDEX IF NOT EXISTS idx_program_dependencies_predecessor ON program_dependencies(predecessor_id);
CREATE INDEX IF NOT EXISTS idx_program_dependencies_successor ON program_dependencies(successor_id);
