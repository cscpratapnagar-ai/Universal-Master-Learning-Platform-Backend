package com.masterlearning.platform.modules.ai.engine;

import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RagContextRetriever {
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;

    public RagContextRetriever(CourseModuleRepository modules, LessonRepository lessons) {
        this.modules = modules;
        this.lessons = lessons;
    }

    public List<Chunk> retrieve(UUID courseId, String query, int limit) {
        Set<String> terms = tokenize(query);
        List<Chunk> candidates = new ArrayList<>();
        modules.findByCourseIdOrderBySortOrderAsc(courseId).forEach(module ->
                lessons.findByModuleIdOrderBySortOrderAsc(module.getId()).forEach(lesson -> {
                    String content = lesson.getContent() == null ? "" : lesson.getContent();
                    String haystack = (lesson.getTitle() + " " + content).toLowerCase(Locale.ROOT);
                    long matches = terms.stream().filter(haystack::contains).count();
                    double relevance = terms.isEmpty() ? 0.0 : (double) matches / terms.size();
                    if (relevance > 0.0 && !content.isBlank()) {
                        candidates.add(new Chunk(lesson.getId(), lesson.getTitle(), content, relevance));
                    }
                }));
        return candidates.stream()
                .sorted(Comparator.comparingDouble(Chunk::relevance).reversed())
                .limit(Math.max(1, limit))
                .toList();
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() >= 3)
                .filter(token -> !Set.of("the", "and", "for", "with", "what", "why", "how", "can", "this", "that").contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public record Chunk(UUID lessonId, String lessonTitle, String content, double relevance) {}
}
