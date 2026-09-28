package com.masterlearning.platform.subscription;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.masterlearning.platform.common.api.ApiResponse;

@RestController
@RequestMapping("/api/v1/public/subscription-plans")
public class SubscriptionPlanController {
    private final SubscriptionPlanService service;

    public SubscriptionPlanController(SubscriptionPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SubscriptionPlanResponse>> getActivePlans() {
        return ApiResponse.success("Subscription plans loaded", service.activePlans());
    }
}

@RestController
@RequestMapping("/api/v1/super-admin/subscription-plans")
@PreAuthorize("hasRole('SUPER_ADMIN')")
class SuperAdminSubscriptionPlanController {
    private final SubscriptionPlanService service;

    SuperAdminSubscriptionPlanController(SubscriptionPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SubscriptionPlanResponse>> getPlans() {
        return ApiResponse.success("Subscription plans loaded", service.activePlans());
    }

    @PutMapping("/{code}")
    public ApiResponse<SubscriptionPlanResponse> updatePlan(
        @PathVariable String code,
        @RequestBody SubscriptionPlanUpdateRequest request
    ) {
        return ApiResponse.success("Subscription plan updated", service.updatePlan(code, request));
    }
}

