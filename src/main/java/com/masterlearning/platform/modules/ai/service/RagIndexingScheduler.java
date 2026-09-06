package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.course.entity.Course;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically refreshes semantic RAG indexes without coupling external embedding
 * calls to course-content write transactions. Unchanged lessons are skipped by
 * SemanticRagService content-version checks.
 */
@Component
public class RagIndexingScheduler {
    private final CourseRepository courses;
    private final SemanticRagService semanticRag;

    public RagIndexingScheduler(CourseRepository courses, SemanticRagService semanticRag) {
        this.courses = courses;
        this.semanticRag = semanticRag;
    }

    @Scheduled(fixedDelayString = "${OPENAI_RAG_REINDEX_DELAY_MS:3600000}")
    public void refreshIndexes() {
        if (!semanticRag.isEmbeddingProviderConfigured()) return;
        List<Course> activeCourses = courses.findAll();
        for (Course course : activeCourses) {
            try {
                semanticRag.indexCourse(course.getId());
            } catch (RuntimeException ignored) {
                // One course must not prevent other courses from being refreshed.
            }
        }
    }
}
