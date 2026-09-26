package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.response.CourseResponse;
import com.masterlearning.platform.modules.course.entity.CourseStatus;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/courses")
public class StudentCourseController {

    private final EnrollmentRepository enrollments;
    private final CourseRepository courses;

    public StudentCourseController(EnrollmentRepository e, CourseRepository c) {
        enrollments = e;
        courses = c;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> myCourses() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        var data = enrollments.findByUserId(currentUserId).stream().map(e -> {
            var c = e.getCourse();
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("enrollmentId", e.getId());
            x.put("courseId", c.getId());
            x.put("title", c.getTitle());
            x.put("description", c.getDescription());
            x.put("status", c.getStatus().name());
            x.put("progressPercent", e.getProgressPercent());
            return x;
        }).toList();

        return ApiResponse.success("Student courses retrieved", data);
    }

    @GetMapping("/catalog")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> catalog() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        var enrolledCourseIds = enrollments.findByUserId(currentUserId).stream()
                .map(e -> e.getCourse().getId())
                .collect(java.util.stream.Collectors.toSet());

        var data = courses.findByStatus(CourseStatus.PUBLISHED).stream().map(c -> {
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("courseId", c.getId());
            x.put("title", c.getTitle());
            x.put("slug", c.getSlug());
            x.put("description", c.getDescription());
            x.put("status", c.getStatus().name());
            x.put("organizationId", c.getOrganization() == null ? null : c.getOrganization().getId());
            x.put("enrolled", enrolledCourseIds.contains(c.getId()));
            return x;
        }).toList();

        return ApiResponse.success("Published course catalog retrieved", data);
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<CourseResponse> course(@PathVariable UUID courseId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        if (!enrollments.existsByCourseIdAndUserId(courseId, currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not enrolled in this course"
            );
        }

        var c = courses.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        return ApiResponse.success("Course retrieved",
                new CourseResponse(c.getId(), c.getTitle(), c.getSlug(), c.getDescription(),
                        c.getStatus().name(),
                        c.getOrganization() == null ? null : c.getOrganization().getId()));
    }
}
