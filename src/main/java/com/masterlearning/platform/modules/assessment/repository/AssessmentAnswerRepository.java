package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, UUID> {
    List<AssessmentAnswer> findByAttemptId(UUID attemptId);

    interface KnowledgeGapProjection {
        UUID getQuestionId();
        String getQuestionText();
        String getAssessmentTitle();
        UUID getLessonId();
        boolean isCorrect();
        int getPointsAwarded();
        int getMaxPoints();
        Instant getSubmittedAt();
    }

    @Query("select q.id as questionId, q.questionText as questionText, " +
           "a.title as assessmentTitle, a.lesson.id as lessonId, aa.correct as correct, " +
           "aa.pointsAwarded as pointsAwarded, q.points as maxPoints, at.submittedAt as submittedAt " +
           "from AssessmentAnswer aa join aa.question q join aa.attempt at join at.assessment a " +
           "where at.user.id = :userId and a.course.id = :courseId order by at.submittedAt desc")
    List<KnowledgeGapProjection> findLearnerKnowledgeEvidence(
            @Param("courseId") UUID courseId,
            @Param("userId") UUID userId);
}
