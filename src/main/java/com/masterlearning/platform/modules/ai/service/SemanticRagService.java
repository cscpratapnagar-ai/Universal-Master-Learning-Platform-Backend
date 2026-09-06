package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.SemanticRagRepository;
import com.masterlearning.platform.modules.course.entity.CourseModule;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SemanticRagService {
    private static final int CHUNK_SIZE = 1200;
    private static final int CHUNK_OVERLAP = 180;

    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final OpenAiEmbeddingService embeddings;
    private final SemanticRagRepository repository;

    public SemanticRagService(
            CourseModuleRepository modules,
            LessonRepository lessons,
            OpenAiEmbeddingService embeddings,
            SemanticRagRepository repository) {
        this.modules = modules;
        this.lessons = lessons;
        this.embeddings = embeddings;
        this.repository = repository;
    }

    public IndexResult indexCourse(UUID courseId) {
        int lessonsIndexed = 0;
        int chunksIndexed = 0;
        for (CourseModule module : modules.findByCourseIdOrderBySortOrderAsc(courseId)) {
            for (Lesson lesson : lessons.findByModuleIdOrderBySortOrderAsc(module.getId())) {
                String content = lesson.getContent() == null ? "" : lesson.getContent().trim();
                if (content.isBlank()) continue;
                List<String> chunks = chunk(content);
                List<SemanticRagRepository.ChunkRow> rows = new ArrayList<>();
                for (int i = 0; i < chunks.size(); i++) {
                    rows.add(new SemanticRagRepository.ChunkRow(i, chunks.get(i), embeddings.embed(chunks.get(i))));
                }
                repository.replaceLessonChunks(courseId, lesson.getId(), rows);
                lessonsIndexed++;
                chunksIndexed += rows.size();
            }
        }
        return new IndexResult(courseId, lessonsIndexed, chunksIndexed);
    }

    public List<SemanticRagRepository.SemanticChunk> search(UUID courseId, String query, int limit) {
        return repository.search(courseId, embeddings.embed(query), limit);
    }

    public long indexedChunkCount(UUID courseId) {
        return repository.countByCourseId(courseId);
    }

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

    public record IndexResult(UUID courseId, int lessonsIndexed, int chunksIndexed) {}
}
