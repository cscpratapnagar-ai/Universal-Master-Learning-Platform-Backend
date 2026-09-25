package com.masterlearning.platform.modules.course.repository;

import com.masterlearning.platform.modules.course.entity.*;
import com.masterlearning.platform.modules.course.dto.response.CourseResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CourseRepository extends JpaRepository<Course,UUID> {
    List<Course> findByStatus(CourseStatus status);
    Optional<Course> findBySlug(String slug);
    List<Course> findByCreatedById(UUID userId);
    @Query("select new com.masterlearning.platform.modules.course.dto.response.CourseResponse(c.id, c.title, c.slug, c.description, c.status, c.organization.id) from Course c where c.organization.id = :organizationId order by c.title asc")
    List<CourseResponse> findCourseResponsesByOrganizationId(@Param("organizationId") UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    long countByOrganizationIdAndStatus(UUID organizationId, CourseStatus status);
}
