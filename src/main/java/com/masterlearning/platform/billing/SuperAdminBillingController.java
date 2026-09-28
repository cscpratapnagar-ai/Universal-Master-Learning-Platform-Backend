package com.masterlearning.platform.billing;

import com.masterlearning.platform.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/super-admin/billing")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminBillingController {
    private final BillingOrderService billing;

    public SuperAdminBillingController(BillingOrderService billing) {
        this.billing = billing;
    }

    @PostMapping("/orders/{orderId}/refund")
    public ApiResponse<BillingRefund> refund(@PathVariable String orderId, @RequestBody RefundRequest request) {
        return ApiResponse.success(
                "Refund processed",
                billing.refund(java.util.UUID.fromString(orderId), request.amount(), request.reason())
        );
    }
}
