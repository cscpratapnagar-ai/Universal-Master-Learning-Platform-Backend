package com.masterlearning.platform.modules.course.security;

import com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseAuthorizationServiceTest {
    private final CourseAuthorizationService service =
            new CourseAuthorizationService(org.mockito.Mockito.mock(OrganizationAuthorizationService.class));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsQuestionSetterAsCourseAuthorRole() {
        authenticate("ROLE_QUESTION_SETTER");
        assertTrue(service.isCourseAuthorOrQuestionSetter());
    }

    @Test
    void allowsTeacherAndInstructorAsCourseAuthorRoles() {
        authenticate("ROLE_TEACHER");
        assertTrue(service.isCourseAuthorOrQuestionSetter());

        authenticate("ROLE_INSTRUCTOR");
        assertTrue(service.isCourseAuthorOrQuestionSetter());
    }

    @Test
    void doesNotAllowLearnerAsCourseAuthor() {
        authenticate("ROLE_STUDENT");
        assertFalse(service.isCourseAuthorOrQuestionSetter());
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test-user", "N/A", List.of(new SimpleGrantedAuthority(role))));
    }
}
