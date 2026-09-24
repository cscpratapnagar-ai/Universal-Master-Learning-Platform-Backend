package com.masterlearning.platform.modules.course.repository;

import com.masterlearning.platform.modules.course.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CourseRepository extends JpaRepository<Course,UUID> {
    List<Course> findByStatus(CourseStatus status);
    Optional<Course> findBySlug(String slug);
    List<Course> findByCreatedById(UUID userId);
    List<Course> findByOrganizationIdOrderByTitleAsc(UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    long countByOrganizationIdAndStatus(UUID organizationId, CourseStatus status);
}
