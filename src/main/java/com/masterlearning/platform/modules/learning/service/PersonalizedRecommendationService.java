package com.masterlearning.platform.modules.learning.service;

import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import com.masterlearning.platform.modules.ai.service.ConceptMasteryService;
import com.masterlearning.platform.modules.course.entity.CourseModule;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.learning.dto.response.LearnerGoalsPreferences;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedDifficultyPace;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedRecommendation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PersonalizedRecommendationService {
    private static final double CRITICAL = 40.0;
    private static final double WEAK = 60.0;
    private static final double ADVANCED = 85.0;

    private final EnrollmentRepository enrollments;
    private final LearningKnowledgeGraphRepository graph;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final ConceptMasteryService conceptMastery;
    private final PersonalizedDifficultyPaceService difficultyPace;
    private final LearnerGoalsPreferencesService goalsPreferences;

    public PersonalizedRecommendationService(EnrollmentRepository enrollments,
                                              LearningKnowledgeGraphRepository graph,
                                              CourseModuleRepository modules,
                                              LessonRepository lessons,
                                              ConceptMasteryService conceptMastery,
                                              PersonalizedDifficultyPaceService difficultyPace,
                                              LearnerGoalsPreferencesService goalsPreferences) {
        this.enrollments = enrollments;
        this.graph = graph;
        this.modules = modules;
        this.lessons = lessons;
        this.conceptMastery = conceptMastery;
        this.difficultyPace = difficultyPace;
        this.goalsPreferences = goalsPreferences;
    }

    public PersonalizedRecommendation forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Enrollment does not belong to current user");
        }

        UUID courseId = enrollment.getCourse().getId();
        double mastery = graph.findLearnerMastery(courseId, userId).stream()
                .mapToDouble(LearningKnowledgeGraphRepository.MasteryRow::mastery)
                .average().orElse(0.0);
        List<String> weakConcepts = conceptMastery.weakConcepts(courseId, userId, WEAK, 3);
        PersonalizedDifficultyPace pace = difficultyPace.forEnrollment(enrollmentId, userId);
        LearnerGoalsPreferences goals = goalsPreferences.forEnrollment(enrollmentId, userId);

        Lesson target = firstLesson(courseId);
        List<String> signals = new ArrayList<>();
        if (!weakConcepts.isEmpty()) signals.add("Weak concepts: " + String.join(", ", weakConcepts));
        signals.add("Difficulty: " + pace.difficulty());
        signals.add("Pace: " + pace.pace());
        signals.add("Goal: " + goals.primaryGoal());

        String action;
        String priority;
        String rationale;
        if (mastery < CRITICAL) {
            action = "REMEDIATE";
            priority = "HIGH";
            rationale = "Critical mastery gaps should be repaired before progression";
        } else if (!weakConcepts.isEmpty() || mastery < WEAK) {
            action = "TARGETED_PRACTICE";
            priority = "HIGH";
            rationale = "Weak concepts should receive targeted reinforcement before acceleration";
        } else if (mastery >= ADVANCED) {
            action = "ACCELERATE";
            priority = "MEDIUM";
            rationale = "Strong mastery supports faster progression and higher challenge";
        } else {
            action = "CONTINUE";
            priority = "MEDIUM";
            rationale = "Mastery is suitable for steady progression through the course sequence";
        }

        if (target == null) {
            action = "COURSE_REVIEW";
            priority = "LOW";
            rationale = "No lesson is currently available; review course state before continuing";
        }

        return new PersonalizedRecommendation(enrollmentId, courseId, action, priority,
                target == null ? null : target.getId(), target == null ? null : target.getTitle(),
                rationale, List.copyOf(signals));
    }

    private Lesson firstLesson(UUID courseId) {
        for (CourseModule module : modules.findByCourseIdOrderBySortOrderAsc(courseId)) {
            List<Lesson> courseLessons = lessons.findByModuleIdOrderBySortOrderAsc(module.getId());
            if (!courseLessons.isEmpty()) return courseLessons.get(0);
        }
        return null;
    }
}
