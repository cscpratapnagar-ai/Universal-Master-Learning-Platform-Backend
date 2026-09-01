package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.response.CourseResponse;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
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
