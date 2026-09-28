package com.masterlearning.platform.subscription;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/subscription-plans")
public class SubscriptionPlanController {
    private final SubscriptionPlanService service;

    public SubscriptionPlanController(SubscriptionPlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<SubscriptionPlanResponse> getActivePlans() {
        return service.activePlans();
    }
}
