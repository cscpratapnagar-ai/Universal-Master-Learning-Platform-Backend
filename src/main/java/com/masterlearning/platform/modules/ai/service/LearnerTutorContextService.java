package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LearnerTutorContextService {
    private static final int WEAK_AREA_THRESHOLD = 60;
    private static final int MAX_WEAK_AREAS = 5;
    private static final int MAX_WEAK_CONCEPTS = 5;

    private final EnrollmentRepository enrollments;
    private final AssessmentAttemptRepository attempts;
    private final ConceptMasteryService conceptMastery;

    public LearnerTutorContextService(EnrollmentRepository enrollments,
                                      AssessmentAttemptRepository attempts,
                                      ConceptMasteryService conceptMastery) {
        this.enrollments = enrollments;
        this.attempts = attempts;
        this.conceptMastery = conceptMastery;
    }

    public LearnerTutorContext build(UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId).orElseThrow();
        UUID userId = enrollment.getUser().getId();
        UUID courseId = enrollment.getCourse().getId();
        List<AssessmentAttempt> history = attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(courseId, userId);

        double mastery = history.stream().mapToInt(AssessmentAttempt::getScore).average().orElse(0.0);
        String state = mastery >= 85 ? "ADVANCED" : mastery >= 70 ? "PROFICIENT" : mastery >= 50 ? "DEVELOPING" : "FOUNDATIONAL";
        String risk = mastery < 50 ? "HIGH" : mastery < 70 ? "MEDIUM" : "LOW";
        String momentum = momentum(history);
        String style = mastery < 50 ? "SIMPLE_STEP_BY_STEP" : mastery >= 85 ? "CHALLENGE_AND_ACCELERATE" : "BALANCED_EXPLANATION";
        List<String> weakAreas = weakAreas(history);
        List<String> weakConcepts = conceptMastery.weakConcepts(courseId, userId, WEAK_AREA_THRESHOLD, MAX_WEAK_CONCEPTS);
        String action = mastery < 50 ? "REMEDIATE" : mastery >= 85 ? "ACCELERATE" : "CONTINUE_LEARNING";

        return new LearnerTutorContext(enrollmentId, round(mastery), state, risk, momentum,
                weakAreas, weakConcepts, action, style);
    }

    static List<String> weakAreas(List<AssessmentAttempt> history) {
        if (history == null || history.isEmpty()) {
            return List.of("No specific weak area identified");
        }

        Set<String> areas = new LinkedHashSet<>();
        Set<UUID> assessed = new LinkedHashSet<>();
        for (AssessmentAttempt attempt : history) {
            if (attempt == null || attempt.getAssessment() == null || !assessed.add(attempt.getAssessment().getId())) {
                continue;
            }
            if (attempt.getScore() < WEAK_AREA_THRESHOLD) {
                String title = attempt.getAssessment().getTitle();
                if (title != null && !title.isBlank()) {
                    areas.add(title.trim());
                }
            }
            if (areas.size() >= MAX_WEAK_AREAS) break;
        }

        return areas.isEmpty() ? List.of("No specific weak area identified") : new ArrayList<>(areas);
    }

    private String momentum(List<AssessmentAttempt> history) {
        if (history.size() < 2) return history.isEmpty() ? "STARTING" : "BUILDING";
        int latest = history.get(0).getScore();
        int previous = history.get(1).getScore();
        if (latest >= previous + 10) return "EXCELLENT";
        if (latest > previous) return "ON_TRACK";
        if (latest == previous) return "BUILDING";
        return "DECLINING";
    }

    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
