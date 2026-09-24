package com.masterlearning.platform.modules.course.security;

import com.masterlearning.platform.modules.course.entity.Course;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourseAuthorizationService {

    public boolean isTeacherOrInstructor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_TEACHER".equals(authority.getAuthority())
                        || "ROLE_INSTRUCTOR".equals(authority.getAuthority()));
    }

    public boolean canManage(Course course) {
        if (course == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        boolean elevated = authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_SUPER_ADMIN".equals(authority.getAuthority())
                        || "ROLE_ADMIN".equals(authority.getAuthority()));
        if (elevated) {
            return true;
        }

        if (!isTeacherOrInstructor() || course.getCreatedBy() == null) {
            return false;
        }

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return currentUserId.equals(course.getCreatedBy().getId());
    }

    public void assertCanManage(Course course) {
        if (!canManage(course)) {
            throw new AccessDeniedException("You do not have access to manage this course");
        }
    }
}
