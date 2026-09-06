package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    Optional<AssessmentAttempt> findTopByAssessmentIdAndUserIdAndPassedTrueOrderBySubmittedAtDesc(UUID assessmentId, UUID userId);
    long countByAssessmentIdAndUserId(UUID assessmentId, UUID userId);
    List<AssessmentAttempt> findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(UUID assessmentId, UUID userId);

    @Query("select a from AssessmentAttempt a where a.assessment.course.id = :courseId and a.user.id = :userId order by a.submittedAt desc")
    List<AssessmentAttempt> findByCourseIdAndUserIdOrderBySubmittedAtDesc(
            @Param("courseId") UUID courseId,
            @Param("userId") UUID userId);
}
