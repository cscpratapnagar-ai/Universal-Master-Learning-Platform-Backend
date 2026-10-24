package com.masterlearning.platform.modules.certificate.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.certificate.entity.Certificate;
import com.masterlearning.platform.modules.certificate.repository.CertificateRepository;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.entity.Course;
import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentCertificateControllerTest {

    private final CertificateRepository certificates = mock(CertificateRepository.class);
    private final CourseRepository courses = mock(CourseRepository.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final StudentCertificateController controller =
            new StudentCertificateController(certificates, courses, enrollments);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsExistingCertificateInsteadOfIssuingAnotherOne() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID certificateId = UUID.randomUUID();
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(courseId);
        Certificate existing = mock(Certificate.class);
        when(existing.getId()).thenReturn(certificateId);
        when(existing.getCertificateNumber()).thenReturn("UMLP-EXISTING");
        when(existing.getCourse()).thenReturn(course);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new CurrentUserPrincipal(userId, "student@example.test"),
                        "N/A",
                        java.util.List.of()));

        when(certificates.findByCourseIdAndUserId(courseId, userId))
                .thenReturn(Optional.of(existing));

        ApiResponse<Map<String, Object>> response = controller.issue(courseId);

        assertTrue(response.success());
        assertEquals("UMLP-EXISTING", response.data().get("certificateNumber"));
        assertEquals(courseId, response.data().get("courseId"));
        verify(certificates, never()).save(any(Certificate.class));
        verifyNoInteractions(courses, enrollments);
    }
}
