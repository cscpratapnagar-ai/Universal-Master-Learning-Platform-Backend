package com.masterlearning.platform.modules.ai.service;
import com.masterlearning.platform.modules.ai.dto.request.LearningInterventionOutcomeRequest;
import com.masterlearning.platform.modules.ai.entity.LearningInterventionOutcome;
import com.masterlearning.platform.modules.ai.repository.LearningInterventionOutcomeRepository;
import com.masterlearning.platform.modules.ai.dto.response.LearningInterventionOutcomeSummary;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class LearningInterventionOutcomeService {
    private final EnrollmentRepository enrollments; private final UserRepository users; private final LearningInterventionOutcomeRepository outcomes;
    public LearningInterventionOutcomeService(EnrollmentRepository enrollments, UserRepository users, LearningInterventionOutcomeRepository outcomes){
        this.enrollments=enrollments; this.users=users; this.outcomes=outcomes;
    }
    public LearningInterventionOutcome record(UUID enrollmentId, UUID userId, LearningInterventionOutcomeRequest request){
        Enrollment enrollment=enrollments.findById(enrollmentId).orElseThrow(()->new EntityNotFoundException("Enrollment not found"));
        if(!enrollment.getUser().getId().equals(userId)) throw new AccessDeniedException("Enrollment does not belong to current user");
        User user=users.findById(userId).orElseThrow(()->new EntityNotFoundException("User not found"));
        if(request.masteryDelta()!=null && (request.masteryDelta() < -100 || request.masteryDelta() > 100))
            throw new IllegalArgumentException("masteryDelta must be between -100 and 100");
        return outcomes.save(new LearningInterventionOutcome(enrollment,user,request.interventionType().trim().toUpperCase(),
            request.targetLessonId(),request.outcome().trim().toUpperCase(),request.masteryDelta(),request.notes()));
    }
    public LearningInterventionOutcomeSummary summary(UUID enrollmentId, UUID userId){
        List<LearningInterventionOutcome> history = recent(enrollmentId, userId);
        long successful = history.stream().filter(o -> "SUCCESS".equalsIgnoreCase(o.getOutcome()) || "IMPROVED".equalsIgnoreCase(o.getOutcome())).count();
        long failed = history.stream().filter(o -> "FAILED".equalsIgnoreCase(o.getOutcome())).count();
        long noImprovement = history.stream().filter(o -> "NO_IMPROVEMENT".equalsIgnoreCase(o.getOutcome())).count();
        long unknown = history.size() - successful - failed - noImprovement;
        double averageDelta = history.stream().map(LearningInterventionOutcome::getMasteryDelta).filter(java.util.Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
        return new LearningInterventionOutcomeSummary(history.size(), successful, failed, noImprovement, unknown, averageDelta);
    }

    public List<LearningInterventionOutcome> recent(UUID enrollmentId, UUID userId){
        Enrollment enrollment=enrollments.findById(enrollmentId).orElseThrow(()->new EntityNotFoundException("Enrollment not found"));
        if(!enrollment.getUser().getId().equals(userId)) throw new AccessDeniedException("Enrollment does not belong to current user");
        return outcomes.findTop20ByEnrollmentIdOrderByCreatedAtDesc(enrollmentId);
    }
}