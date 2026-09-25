package com.masterlearning.platform.modules.course.repository;

import com.masterlearning.platform.modules.course.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CourseRepository extends JpaRepository<Course,UUID> {
    List<Course> findByStatus(CourseStatus status);
    Optional<Course> findBySlug(String slug);
    @EntityGraph(attributePaths = "organization")
    List<Course> findByOrganizationIdOrderByTitleAsc(UUID organizationId);
    List<Course> findByCreatedById(UUID userId);
    long countByOrganizationId(UUID organizationId);
    long countByOrganizationIdAndStatus(UUID organizationId, CourseStatus status);
}
