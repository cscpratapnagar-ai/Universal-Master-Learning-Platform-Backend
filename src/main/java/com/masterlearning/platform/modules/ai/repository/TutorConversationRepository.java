package com.masterlearning.platform.modules.ai.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TutorConversationRepository {
    private static final int MAX_TURNS = 8;
    private final JdbcTemplate jdbcTemplate;

    public TutorConversationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> recent(UUID enrollmentId) {
        if (enrollmentId == null) return List.of();
        List<String> rows = jdbcTemplate.query(
                "SELECT role, content FROM ai_tutor_conversation_turns WHERE enrollment_id = ? "
                        + "ORDER BY created_at DESC LIMIT ?",
                (rs, rowNum) -> rs.getString("role") + ": " + rs.getString("content"),
                enrollmentId, MAX_TURNS * 2);
        java.util.Collections.reverse(rows);
        return rows;
    }

    public void remember(UUID enrollmentId, String role, String content) {
        if (enrollmentId == null || content == null || content.isBlank()) return;
        jdbcTemplate.update(
                "INSERT INTO ai_tutor_conversation_turns (id, enrollment_id, role, content) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), enrollmentId, role, safe(content));
        jdbcTemplate.update(
                "DELETE FROM ai_tutor_conversation_turns WHERE enrollment_id = ? AND id NOT IN "
                        + "(SELECT id FROM ai_tutor_conversation_turns WHERE enrollment_id = ? ORDER BY created_at DESC LIMIT ?)",
                enrollmentId, enrollmentId, MAX_TURNS * 2);
    }

    private String safe(String value) {
        return value.length() > 1500 ? value.substring(0, 1500) : value;
    }
}
