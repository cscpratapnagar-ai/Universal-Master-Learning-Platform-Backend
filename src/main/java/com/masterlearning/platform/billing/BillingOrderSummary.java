package com.masterlearning.platform.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BillingOrderSummary(
        UUID id, UUID userId, UUID planId, String billingCycle, BigDecimal amount,
        String currency, String status, String externalOrderId,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static BillingOrderSummary from(BillingOrder order) {
        return new BillingOrderSummary(order.getId(), order.getUserId(), order.getPlanId(),
                order.getBillingCycle(), order.getAmount(), order.getCurrency(), order.getStatus(),
                order.getExternalOrderId(), order.getCreatedAt(), order.getUpdatedAt());
    }
}