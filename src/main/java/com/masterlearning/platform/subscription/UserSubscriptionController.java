package com.masterlearning.platform.subscription;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/subscription")
@PreAuthorize("isAuthenticated()")
public class UserSubscriptionController {
    private final UserSubscriptionService service;

    public UserSubscriptionController(UserSubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ApiResponse<UserSubscriptionResponse> current() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success("Current subscription loaded", service.current(userId));
    }
}
