package com.masterlearning.platform.modules.certificate.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.certificate.entity.Certificate;
import com.masterlearning.platform.modules.certificate.repository.CertificateRepository;
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
@RequestMapping("/api/v1/student/certificates")
public class StudentCertificateController {

    private final CertificateRepository certificates;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public StudentCertificateController(CertificateRepository certificates, CourseRepository courses, EnrollmentRepository enrollments) {
        this.certificates = certificates;
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> mine() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success("Student certificates retrieved",
                certificates.findByUserId(userId).stream().map(this::view).toList());
    }

    @PostMapping("/courses/{courseId}/issue")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ApiResponse<Map<String, Object>> issue(@PathVariable UUID courseId) {
        UUID userId = SecurityUtils.getCurrentUserId();

        var existing = certificates.findByCourseIdAndUserId(courseId, userId);
        if (existing.isPresent()) return ApiResponse.success("Certificate already issued", view(existing.get()));

        var course = courses.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        var enrollment = enrollments.findByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (enrollment.getProgressPercent() < 100) {
            throw new IllegalArgumentException("Course must be completed before certificate issuance");
        }

        String number = "UMLP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Certificate certificate = certificates.save(new Certificate(number, course, 
                enrollment.getUser()));

        return ApiResponse.success("Certificate issued", view(certificate));
    }

    private Map<String, Object> view(Certificate c) {
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("id", c.getId());
        x.put("certificateNumber", c.getCertificateNumber());
        x.put("courseId", c.getCourse().getId());
        return x;
    }
}
