package com.masterlearning.platform.modules.course.repository;

import com.masterlearning.platform.modules.course.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CourseRepository extends JpaRepository<Course,UUID> {
    List<Course> findByStatus(CourseStatus status);
    Optional<Course> findBySlug(String slug);
    List<Course> findByCreatedById(UUID userId);
    @Query("select c from Course c join fetch c.organization where c.organization.id = :organizationId order by c.title asc")
    List<Course> findByOrganizationIdOrderByTitleAsc(@Param("organizationId") UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    long countByOrganizationIdAndStatus(UUID organizationId, CourseStatus status);
}
