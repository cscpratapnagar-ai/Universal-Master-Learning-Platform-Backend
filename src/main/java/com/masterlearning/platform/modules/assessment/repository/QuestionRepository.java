package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByAssessmentId(UUID assessmentId);
    boolean existsByIdAndAssessmentId(UUID questionId, UUID assessmentId);
    List<Question> findByAssessmentCourseId(UUID courseId);
    @Query("select q from Question q join q.assessment a join a.course c where c.organization.id = :organizationId")
    List<Question> findByOrganizationId(@Param("organizationId") UUID organizationId);
}
