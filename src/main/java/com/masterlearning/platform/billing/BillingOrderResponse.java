package com.masterlearning.platform.billing;

import java.math.BigDecimal;
import java.util.UUID;

public record BillingOrderResponse(
        UUID orderId,
        String planCode,
        String planName,
        String billingCycle,
        BigDecimal amount,
        String currency,
        String status,
        String paymentProvider
) {}
