-- Track the exact lesson content version used to generate tutor embeddings.
ALTER TABLE ai_tutor_chunks ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);

-- Existing rows predate content hashing and must be regenerated once.
