package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.LearnerPersonalization;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LearnerPersonalizationService {
    private final JdbcTemplate jdbc;
    private final EnrollmentRepository enrollments;

    public LearnerPersonalizationService(JdbcTemplate jdbc, EnrollmentRepository enrollments) {
        this.jdbc = jdbc;
        this.enrollments = enrollments;
    }

    public LearnerPersonalization forEnrollment(UUID enrollmentId, UUID userId) {
        EnrollmentRow enrollment = jdbc.query("SELECT id, course_id, user_id FROM enrollments WHERE id=?",
                (rs,n) -> new EnrollmentRow(rs.getObject("id", UUID.class), rs.getObject("course_id", UUID.class), rs.getObject("user_id", UUID.class)), enrollmentId)
                .stream().findFirst().orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!userId.equals(enrollment.userId())) throw new AccessDeniedException("You do not own this enrollment");
        if (!enrollments.existsByCourseIdAndUserId(enrollment.courseId(), userId)) throw new AccessDeniedException("You are not enrolled in this course");

        Stats stats = jdbc.query("""
                SELECT COALESCE(AVG(at.score),0) mastery,
                       COUNT(at.id) attempts,
                       COALESCE(SUM(CASE WHEN at.passed THEN 1 ELSE 0 END),0) passed
                FROM assessment_attempts at
                JOIN assessments a ON a.id=at.assessment_id
                WHERE a.course_id=? AND at.user_id=?
                """, (rs,n) -> new Stats(rs.getDouble("mastery"), rs.getLong("attempts"), rs.getLong("passed")), enrollment.courseId(), userId)
                .stream().findFirst().orElse(new Stats(0,0,0));

        long weak = jdbc.queryForObject("""
                SELECT COUNT(*) FROM learning_concepts c
                WHERE c.course_id=? AND c.active=TRUE
                  AND EXISTS (SELECT 1 FROM question_concepts qc JOIN assessment_questions q ON q.id=qc.question_id JOIN assessment_answers aa ON aa.question_id=q.id JOIN assessment_attempts at ON at.id=aa.attempt_id JOIN assessments a ON a.id=at.assessment_id WHERE qc.concept_id=c.id AND a.course_id=? AND at.user_id=?)
                  AND (SELECT COALESCE(100.0*SUM(CASE WHEN aa2.correct THEN 1 ELSE 0 END)/NULLIF(COUNT(aa2.id),0),0) FROM question_concepts qc2 JOIN assessment_questions q2 ON q2.id=qc2.question_id JOIN assessment_answers aa2 ON aa2.question_id=q2.id JOIN assessment_attempts at2 ON at2.id=aa2.attempt_id JOIN assessments a2 ON a2.id=at2.assessment_id WHERE qc2.concept_id=c.id AND a2.course_id=? AND at2.user_id=?) < 60
                """, Long.class, enrollment.courseId(), enrollment.courseId(), userId, enrollment.courseId(), userId);

        String risk = stats.mastery < 50 ? "HIGH" : stats.mastery < 70 ? "MEDIUM" : "LOW";
        String state = stats.mastery >= 85 ? "ADVANCED" : stats.mastery >= 70 ? "PROFICIENT" : stats.mastery >= 50 ? "DEVELOPING" : stats.attempts > 0 ? "AT_RISK" : "STARTING";
        String momentum = stats.attempts == 0 ? "STARTING" : stats.passed * 2 >= stats.attempts ? "POSITIVE" : "NEEDS_SUPPORT";
        String priority = weak > 0 || stats.mastery < 60 ? "MASTERY" : stats.mastery >= 85 ? "ADVANCEMENT" : "PROGRESS";
        String action = "MASTERY".equals(priority) ? "TARGETED_PRACTICE" : "ADVANCEMENT".equals(priority) ? "CHALLENGE" : "CONTINUE_LEARNING";
        List<String> reasons = new ArrayList<>();
        if (weak > 0) reasons.add("Weak concepts require targeted practice");
        if (risk.equals("HIGH")) reasons.add("Low overall mastery increases learning risk");
        if (stats.mastery >= 85) reasons.add("Strong mastery supports advancement");
        if (reasons.isEmpty()) reasons.add("Current mastery supports steady progress");
        return new LearnerPersonalization(enrollmentId, enrollment.courseId(), round(stats.mastery), state, risk, momentum, priority, action, List.of(weak > 0 ? "Weak concepts" : "Core course progression"), List.copyOf(reasons));
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
    private record EnrollmentRow(UUID id, UUID courseId, UUID userId) {}
    private record Stats(double mastery, long attempts, long passed) {}
}
