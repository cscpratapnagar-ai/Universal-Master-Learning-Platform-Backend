package com.masterlearning.platform.modules.learning.service;

import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.learning.dto.response.LearnerGoalsPreferences;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizationOrchestration;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedDifficultyPace;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedRecommendation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PersonalizationOrchestrationService {
    private final EnrollmentRepository enrollments;
    private final PersonalizedRecommendationService recommendations;
    private final PersonalizedDifficultyPaceService difficultyPace;
    private final LearnerGoalsPreferencesService goalsPreferences;

    public PersonalizationOrchestrationService(EnrollmentRepository enrollments,
                                               PersonalizedRecommendationService recommendations,
                                               PersonalizedDifficultyPaceService difficultyPace,
                                               LearnerGoalsPreferencesService goalsPreferences) {
        this.enrollments = enrollments;
        this.recommendations = recommendations;
        this.difficultyPace = difficultyPace;
        this.goalsPreferences = goalsPreferences;
    }

    public PersonalizationOrchestration forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Enrollment does not belong to current user");
        }

        PersonalizedRecommendation recommendation = recommendations.forEnrollment(enrollmentId, userId);
        PersonalizedDifficultyPace pace = difficultyPace.forEnrollment(enrollmentId, userId);
        LearnerGoalsPreferences goals = goalsPreferences.forEnrollment(enrollmentId, userId);

        List<String> signals = new ArrayList<>(recommendation.signals());
        signals.add("Recommendation priority: " + recommendation.priority());
        signals.add("Primary goal: " + goals.primaryGoal());
        signals.add("Recommended pace: " + pace.pace());

        return new PersonalizationOrchestration(
                enrollmentId,
                enrollment.getCourse().getId(),
                recommendation.action(),
                recommendation.priority(),
                recommendation.targetLessonId(),
                recommendation.targetLessonTitle(),
                pace.difficulty(),
                pace.pace(),
                goals.primaryGoal(),
                recommendation.rationale(),
                List.copyOf(signals)
        );
    }
}
