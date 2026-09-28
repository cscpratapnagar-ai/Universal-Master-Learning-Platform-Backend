package com.masterlearning.platform.billing;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/billing")
@PreAuthorize("isAuthenticated()")
public class BillingOrderController {
    private final BillingOrderService service;

    public BillingOrderController(BillingOrderService service) {
        this.service = service;
    }

    @PostMapping("/orders")
    public ApiResponse<BillingOrderResponse> createOrder(@RequestBody CreateBillingOrderRequest request) {
        return ApiResponse.success(
                "Checkout order created",
                service.create(SecurityUtils.getCurrentUserId(), request)
        );
    }
}
