package com.masterlearning.platform.modules.learning.service;

import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedLearningPath;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonalizedLearningPathService {
    private final EnrollmentRepository enrollments;
    private final LessonRepository lessons;
    private final LearningKnowledgeGraphRepository graph;

    public PersonalizedLearningPathService(EnrollmentRepository enrollments, LessonRepository lessons,
                                           LearningKnowledgeGraphRepository graph) {
        this.enrollments = enrollments;
        this.lessons = lessons;
        this.graph = graph;
    }

    public PersonalizedLearningPath forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Enrollment does not belong to current user");
        }
        UUID courseId = enrollment.getCourse().getId();
        List<Lesson> courseLessons = new ArrayList<>();
        enrollment.getCourse().getModules().stream()
                .sorted(Comparator.comparingInt(m -> m.getSortOrder()))
                .forEach(module -> courseLessons.addAll(lessons.findByModuleIdOrderBySortOrderAsc(module.getId())));
        Map<UUID, Double> mastery = graph.findLearnerMastery(courseId, userId).stream()
                .collect(java.util.stream.Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id,
                        LearningKnowledgeGraphRepository.MasteryRow::mastery));

        List<PersonalizedLearningPath.Step> steps = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        int priority = 1;
        for (Lesson lesson : courseLessons) {
            String action = priority <= 3 ? "PRIORITIZE" : "SEQUENCE";
            String reason = "Continue course sequence";
            steps.add(new PersonalizedLearningPath.Step(lesson.getId(), lesson.getTitle(), action, priority++, reason));
        }
        if (!mastery.isEmpty()) reasons.add("Learner mastery informs personalization");
        reasons.add("Course sequence preserves prerequisite-safe progression");
        return new PersonalizedLearningPath(enrollmentId, courseId,
                mastery.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0) < 60 ? "REMEDIATION_FIRST" : "BALANCED_PROGRESSION",
                List.copyOf(steps), List.copyOf(reasons));
    }
}
