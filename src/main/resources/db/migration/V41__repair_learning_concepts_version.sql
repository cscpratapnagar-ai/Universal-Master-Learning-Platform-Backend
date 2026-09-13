-- Repair legacy learning_concepts tables that predate BaseEntity optimistic locking.
-- LearningConcept extends BaseEntity, which requires the version column.
ALTER TABLE learning_concepts
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
