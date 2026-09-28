package com.masterlearning.platform.subscription;

import java.time.LocalDate;

public record UserSubscriptionResponse(
        String planCode,
        String planName,
        String status,
        String billingCycle,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        boolean active
) {}
