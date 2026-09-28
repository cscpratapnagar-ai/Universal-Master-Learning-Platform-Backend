package com.masterlearning.platform.billing;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.audit.AuditLogService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/super-admin/billing")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminBillingController {
    private final BillingOrderService billing;
    private final AuditLogService audit;

    public SuperAdminBillingController(BillingOrderService billing, AuditLogService audit) {
        this.billing = billing;
        this.audit = audit;
    }

    @GetMapping("/orders")
    public ApiResponse<java.util.List<BillingOrderSummary>> orders() {
        return ApiResponse.success("Billing orders loaded", billing.allOrders());
    }

    @PostMapping("/orders/{orderId}/refund")
    public ApiResponse<BillingRefund> refund(@PathVariable String orderId, @RequestBody RefundRequest request) {
        var actor = SecurityUtils.getCurrentUserId();
        try {
            var refund = billing.refund(java.util.UUID.fromString(orderId), request.amount(), request.reason());
            audit.success(actor, "BILLING_REFUND", "BILLING_ORDER", orderId, "Refund processed");
            return ApiResponse.success("Refund processed", refund);
        } catch (RuntimeException ex) {
            audit.failure(actor, "BILLING_REFUND", "BILLING_ORDER", orderId, ex.getMessage());
            throw ex;
        }
    }
}
