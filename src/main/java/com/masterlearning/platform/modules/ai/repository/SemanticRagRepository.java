package com.masterlearning.platform.modules.ai.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SemanticRagRepository {
    private final JdbcTemplate jdbcTemplate;

    public SemanticRagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void replaceLessonChunks(UUID courseId, UUID lessonId, List<ChunkRow> chunks) {
        jdbcTemplate.update("DELETE FROM ai_tutor_chunks WHERE lesson_id = ?", lessonId);
        for (ChunkRow chunk : chunks) {
            jdbcTemplate.update(
                    "INSERT INTO ai_tutor_chunks (id, course_id, lesson_id, chunk_index, content, content_hash, embedding) VALUES (?, ?, ?, ?, ?, ?, ?::vector)",
                    UUID.randomUUID(), courseId, lessonId, chunk.index(), chunk.content(), chunk.contentHash(), vectorLiteral(chunk.embedding()));
        }
    }

    public String indexedContentHash(UUID lessonId) {
        return jdbcTemplate.query(
                "SELECT content_hash FROM ai_tutor_chunks WHERE lesson_id = ? ORDER BY chunk_index LIMIT 1",
                (rs, rowNum) -> rs.getString("content_hash"), lessonId)
                .stream().findFirst().orElse(null);
    }

    public List<SemanticChunk> search(UUID courseId, List<Double> embedding, int limit) {
        String sql = "SELECT c.lesson_id, l.title, c.content, 1 - (c.embedding <=> ?::vector) AS relevance "
                + "FROM ai_tutor_chunks c JOIN lessons l ON l.id = c.lesson_id "
                + "WHERE c.course_id = ? "
                + "ORDER BY c.embedding <=> ?::vector LIMIT ?";
        String vector = vectorLiteral(embedding);
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new SemanticChunk(
                        rs.getObject("lesson_id", UUID.class), rs.getString("title"),
                        rs.getString("content"), rs.getDouble("relevance")),
                vector, courseId, vector, limit);
    }

    public long countByCourseId(UUID courseId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_tutor_chunks WHERE course_id = ?", Long.class, courseId);
        return count == null ? 0L : count;
    }

    private String vectorLiteral(List<Double> vector) { return vector.toString().replace(" ", ""); }

    public record ChunkRow(int index, String content, String contentHash, List<Double> embedding) {}
    public record SemanticChunk(UUID lessonId, String lessonTitle, String content, double relevance) {}
}
