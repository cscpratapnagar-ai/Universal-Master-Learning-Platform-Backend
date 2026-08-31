package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.response.CourseResponse;
import com.masterlearning.platform.modules.course.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/student/courses")
public class StudentCourseController {

    private final EnrollmentRepository enrollments;
    private final CourseRepository courses;

    public StudentCourseController(EnrollmentRepository e, CourseRepository c) {
        enrollments = e;
        courses = c;
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<List<Map<String, Object>>> myCourses(@PathVariable UUID userId) {
        var data = enrollments.findByUserId(userId).stream().map(e -> {
            var c = courses.findById(e.getCourse().getId()).orElseThrow();
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
    public ApiResponse<CourseResponse> course(@PathVariable UUID courseId) {
        var c = courses.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found"));
        return ApiResponse.success("Course retrieved", new CourseResponse(c.getId(), c.getTitle(), c.getSlug(), c.getDescription(), c.getStatus().name(), c.getOrganization() == null ? null : c.getOrganization().getId()));
    }
}