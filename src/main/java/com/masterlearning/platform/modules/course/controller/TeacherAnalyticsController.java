package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
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
public class TeacherAnalyticsController {

    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final AssessmentRepository assessments;

    public TeacherAnalyticsController(CourseRepository courses,
                                      EnrollmentRepository enrollments,
                                      AssessmentRepository assessments) {
        this.courses = courses;
        this.enrollments = enrollments;
        this.assessments = assessments;
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('TEACHER','INSTRUCTOR','ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> analytics() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        boolean elevated = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));

        var teacherCourses = elevated ? courses.findAll() : courses.findByCreatedById(currentUserId);
        List<UUID> courseIds = teacherCourses.stream().map(c -> c.getId()).toList();
        List<Enrollment> learnerEnrollments = courseIds.isEmpty()
                ? List.of()
                : enrollments.findByCourseIdIn(courseIds);

        double completionRate = learnerEnrollments.isEmpty()
                ? 0.0
                : learnerEnrollments.stream().mapToInt(Enrollment::getProgressPercent).average().orElse(0.0);

        long assessmentCount = courseIds.stream()
                .mapToLong(id -> assessments.findByCourseId(id).size())
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseCount", teacherCourses.size());
        result.put("learnerCount", learnerEnrollments.stream().map(e -> e.getUser().getId()).distinct().count());
        result.put("assessmentCount", assessmentCount);
        result.put("completionRate", Math.round(completionRate));
        result.put("publishedCourseCount", teacherCourses.stream().filter(c -> "PUBLISHED".equals(c.getStatus().name())).count());
        result.put("draftCourseCount", teacherCourses.stream().filter(c -> "DRAFT".equals(c.getStatus().name())).count());
        return ApiResponse.success("Teacher analytics retrieved", result);
    }
}
