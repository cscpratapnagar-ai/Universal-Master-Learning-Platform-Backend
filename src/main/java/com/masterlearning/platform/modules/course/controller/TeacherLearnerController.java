package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherLearnerController {

    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public TeacherLearnerController(CourseRepository courses, EnrollmentRepository enrollments) {
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @GetMapping("/learners")
    @PreAuthorize("hasAnyRole('TEACHER','INSTRUCTOR','ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> learners() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        boolean elevated = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));

        var teacherCourses = elevated ? courses.findAll() : courses.findByCreatedById(currentUserId);
        List<UUID> courseIds = teacherCourses.stream().map(c -> c.getId()).toList();

        if (courseIds.isEmpty()) {
            return ApiResponse.success("Teacher learners retrieved", List.of());
        }

        List<Map<String, Object>> result = enrollments.findByCourseIdIn(courseIds).stream()
                .map(this::toView)
                .toList();

        return ApiResponse.success("Teacher learners retrieved", result);
    }

    private Map<String, Object> toView(Enrollment enrollment) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("enrollmentId", enrollment.getId());
        view.put("courseId", enrollment.getCourse().getId());
        view.put("courseTitle", enrollment.getCourse().getTitle());
        view.put("learnerId", enrollment.getUser().getId());
        view.put("learnerName", (enrollment.getUser().getFirstName() + " " +
                (enrollment.getUser().getLastName() == null ? "" : enrollment.getUser().getLastName())).trim());
        view.put("learnerEmail", enrollment.getUser().getEmail());
        view.put("progressPercent", enrollment.getProgressPercent());
        view.put("completed", enrollment.isCompleted());
        view.put("completedAt", enrollment.getCompletedAt());
        return view;
    }
}
