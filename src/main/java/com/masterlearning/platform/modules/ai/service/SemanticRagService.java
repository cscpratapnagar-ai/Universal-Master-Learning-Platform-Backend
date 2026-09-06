package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.SemanticRagRepository;
import com.masterlearning.platform.modules.course.entity.CourseModule;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class SemanticRagService {
    private static final int CHUNK_SIZE = 1200;
    private static final int CHUNK_OVERLAP = 180;
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;

    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final OpenAiEmbeddingService embeddings;
    private final SemanticRagRepository repository;
    private final double minimumRelevance;

    public SemanticRagService(CourseModuleRepository modules, LessonRepository lessons,
                              OpenAiEmbeddingService embeddings, SemanticRagRepository repository,
                              @Value("${OPENAI_RAG_MIN_RELEVANCE:0.35}") double minimumRelevance) {
        this.modules = modules;
        this.lessons = lessons;
        this.embeddings = embeddings;
        this.repository = repository;
        this.minimumRelevance = Math.max(0.0, Math.min(1.0, minimumRelevance));
    }

    public IndexResult indexCourse(UUID courseId) {
        int lessonsIndexed = 0;
        int chunksIndexed = 0;
        int lessonsSkipped = 0;
        for (CourseModule module : modules.findByCourseIdOrderBySortOrderAsc(courseId)) {
            for (Lesson lesson : lessons.findByModuleIdOrderBySortOrderAsc(module.getId())) {
                String content = lesson.getContent() == null ? "" : lesson.getContent().trim();
                if (content.isBlank()) continue;
                String contentHash = hash(content);
                if (contentHash.equals(repository.indexedContentHash(lesson.getId()))) {
                    lessonsSkipped++;
                    continue;
                }
                List<String> chunks = chunk(content);
                List<SemanticRagRepository.ChunkRow> rows = new ArrayList<>();
                for (int i = 0; i < chunks.size(); i++) {
                    rows.add(new SemanticRagRepository.ChunkRow(i, chunks.get(i), contentHash, embeddings.embed(chunks.get(i))));
                }
                repository.replaceLessonChunks(courseId, lesson.getId(), rows);
                lessonsIndexed++;
                chunksIndexed += rows.size();
            }
        }
        return new IndexResult(courseId, lessonsIndexed, chunksIndexed, lessonsSkipped);
    }

    public List<SemanticRagRepository.SemanticChunk> search(UUID courseId, String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        int safeLimit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        return repository.search(courseId, embeddings.embed(query.trim()), safeLimit).stream()
                .filter(chunk -> chunk.relevance() >= minimumRelevance).toList();
    }

    public long indexedChunkCount(UUID courseId) { return repository.countByCourseId(courseId); }

    private List<String> chunk(String content) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + CHUNK_SIZE);
            if (end < content.length()) {
                int boundary = content.lastIndexOf(' ', end);
                if (boundary > start + CHUNK_SIZE / 2) end = boundary;
            }
            String piece = content.substring(start, end).trim();
            if (!piece.isBlank()) result.add(piece);
            if (end >= content.length()) break;
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }
        return result;
    }

    private String hash(String content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public record IndexResult(UUID courseId, int lessonsIndexed, int chunksIndexed, int lessonsSkipped) {}
}
