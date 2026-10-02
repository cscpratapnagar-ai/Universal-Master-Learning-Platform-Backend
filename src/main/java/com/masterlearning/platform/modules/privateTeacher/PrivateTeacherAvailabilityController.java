package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/private-teacher")
public class PrivateTeacherAvailabilityController {
    private final PrivateTeacherAvailabilityService service;
    private final UserRepository users;

    public PrivateTeacherAvailabilityController(PrivateTeacherAvailabilityService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping("/{teacherId}/availability")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_ORG_ADMIN','ROLE_TEACHER','ROLE_INSTRUCTOR','ROLE_LEARNER','ROLE_STUDENT')")
    public ApiResponse<List<PrivateTeacherAvailabilityResponse>> get(@PathVariable UUID teacherId) {
        return ApiResponse.success("Private teacher availability loaded", service.get(teacherId));
    }

    @GetMapping("/availability/me")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER','ROLE_INSTRUCTOR')")
    public ApiResponse<List<PrivateTeacherAvailabilityResponse>> getMine(@AuthenticationPrincipal UserDetails principal) {
        UUID teacherId = currentUserId(principal);
        return ApiResponse.success("Private teacher availability loaded", service.get(teacherId));
    }

    @PostMapping("/availability/me")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER','ROLE_INSTRUCTOR')")
    public ApiResponse<PrivateTeacherAvailabilityResponse> createMine(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PrivateTeacherAvailabilityRequest request) {
        UUID teacherId = currentUserId(principal);
        return ApiResponse.success("Private teacher availability created", service.create(teacherId, request));
    }

    @DeleteMapping("/availability/me/{availabilityId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER','ROLE_INSTRUCTOR')")
    public ApiResponse<Void> disableMine(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID availabilityId) {
        UUID teacherId = currentUserId(principal);
        service.disable(teacherId, availabilityId);
        return ApiResponse.success("Private teacher availability disabled", null);
    }

    private UUID currentUserId(UserDetails principal) {
        return users.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated teacher not found"))
                .getId();
    }
}
