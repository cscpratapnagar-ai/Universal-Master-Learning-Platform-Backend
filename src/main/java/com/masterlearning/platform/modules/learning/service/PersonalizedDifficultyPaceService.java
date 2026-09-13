package com.masterlearning.platform.modules.learning.service;

import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedDifficultyPace;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonalizedDifficultyPaceService {
    private final EnrollmentRepository enrollments;
    private final LearningKnowledgeGraphRepository graph;

    public PersonalizedDifficultyPaceService(EnrollmentRepository enrollments, LearningKnowledgeGraphRepository graph) {
        this.enrollments = enrollments;
        this.graph = graph;
    }

    public PersonalizedDifficultyPace forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Enrollment does not belong to current user");
        }
        UUID courseId = enrollment.getCourse().getId();
        double mastery = graph.findLearnerMastery(courseId, userId).stream()
                .mapToDouble(LearningKnowledgeGraphRepository.MasteryRow::mastery)
                .average().orElse(0.0);
        String difficulty = mastery < 50 ? "EASY" : mastery >= 85 ? "HARD" : "MEDIUM";
        String pace = mastery < 50 ? "SLOW" : mastery >= 85 ? "FAST" : "STEADY";
        int dailyLessons = mastery < 50 ? 1 : mastery >= 85 ? 3 : 2;
        List<String> signals = new ArrayList<>();
        if (mastery < 50) signals.add("Low mastery: reduce cognitive load and reinforce foundations");
        else if (mastery >= 85) signals.add("High mastery: accelerate progression and add challenge");
        else signals.add("Developing/proficient mastery: maintain steady progression");
        return new PersonalizedDifficultyPace(enrollmentId, courseId, difficulty, pace, dailyLessons,
                "Difficulty and pace are derived from current learner mastery", List.copyOf(signals));
    }
}
