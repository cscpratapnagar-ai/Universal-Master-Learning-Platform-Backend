package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    List<Assessment> findByLessonId(UUID lessonId);
    List<Assessment> findByCourseId(UUID courseId);
    long countByCourseId(UUID courseId);
    @Query("select count(a) from Assessment a join a.course c where c.organization.id = :organizationId")
    long countByOrganizationId(UUID organizationId);
    @Query("select a from Assessment a join fetch a.course c where c.organization.id = :organizationId")
    List<Assessment> findByOrganizationId(UUID organizationId);
}
