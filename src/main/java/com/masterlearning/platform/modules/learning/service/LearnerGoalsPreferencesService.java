package com.masterlearning.platform.modules.learning.service;

import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.learning.dto.response.LearnerGoalsPreferences;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LearnerGoalsPreferencesService {
    private final EnrollmentRepository enrollments;

    public LearnerGoalsPreferencesService(EnrollmentRepository enrollments) {
        this.enrollments = enrollments;
    }

    public LearnerGoalsPreferences forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Enrollment does not belong to current user");
        }
        return new LearnerGoalsPreferences(
                enrollmentId,
                enrollment.getCourse().getId(),
                "MASTER_COURSE",
                "COURSE_COMPLETION",
                "ADAPTIVE",
                "STEADY",
                List.of("PREREQUISITE_SAFE", "MASTERY_AWARE", "PRACTICE_WHEN_NEEDED"),
                List.of("Goal preferences are ready for learner-provided personalization", "Current course progression remains mastery-aware")
        );
    }
}
