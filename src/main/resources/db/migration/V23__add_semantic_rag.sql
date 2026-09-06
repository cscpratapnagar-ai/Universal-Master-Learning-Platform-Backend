CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS ai_tutor_chunks (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id),
    lesson_id UUID NOT NULL REFERENCES lessons(id),
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ai_tutor_chunk UNIQUE (lesson_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_ai_tutor_chunks_course ON ai_tutor_chunks(course_id);
CREATE INDEX IF NOT EXISTS idx_ai_tutor_chunks_lesson ON ai_tutor_chunks(lesson_id);
CREATE INDEX IF NOT EXISTS idx_ai_tutor_chunks_embedding_hnsw
    ON ai_tutor_chunks USING hnsw (embedding vector_cosine_ops);
