package com.masterlearning.platform.subscription;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
