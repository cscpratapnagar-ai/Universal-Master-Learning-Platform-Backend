package com.masterlearning.platform.billing;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/invoices")
    public ApiResponse<java.util.List<BillingInvoice>> invoices() {
        return ApiResponse.success("Invoices loaded", service.invoices(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders")
    public ApiResponse<BillingOrderResponse> createOrder(@RequestBody CreateBillingOrderRequest request) {
        return ApiResponse.success(
                "Checkout order created",
                service.create(SecurityUtils.getCurrentUserId(), request)
        );
    }

    @PostMapping("/orders/{orderId}/verify")
    public ApiResponse<Void> verifyPayment(
            @PathVariable String orderId,
            @RequestBody VerifyRazorpayPaymentRequest request) {
        service.verifyPayment(
                SecurityUtils.getCurrentUserId(),
                orderId,
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );
        return ApiResponse.success("Payment verified and subscription activated", null);
    }

    @PostMapping("/webhooks/razorpay")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> razorpayWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String rawBody) {
        service.handleRazorpayWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
