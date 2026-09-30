package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/private-teacher/{teacherId}/availability")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ORG_ADMIN', 'ROLE_TEACHER', 'ROLE_INSTRUCTOR')")
public class PrivateTeacherAvailabilityController {
    private final PrivateTeacherAvailabilityService service;

    public PrivateTeacherAvailabilityController(PrivateTeacherAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<PrivateTeacherAvailabilityResponse>> get(@PathVariable UUID teacherId) {
        return ApiResponse.success("Private teacher availability loaded", service.get(teacherId));
    }

    @PostMapping
    public ApiResponse<PrivateTeacherAvailabilityResponse> create(
            @PathVariable UUID teacherId,
            @Valid @RequestBody PrivateTeacherAvailabilityRequest request) {
        return ApiResponse.success("Private teacher availability created", service.create(teacherId, request));
    }

    @DeleteMapping("/{availabilityId}")
    public ApiResponse<Void> disable(@PathVariable UUID teacherId, @PathVariable UUID availabilityId) {
        service.disable(teacherId, availabilityId);
        return ApiResponse.success("Private teacher availability disabled", null);
    }
}
