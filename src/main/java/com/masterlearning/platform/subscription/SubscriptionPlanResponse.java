package com.masterlearning.platform.subscription;

import java.math.BigDecimal;
import java.util.Map;

public record SubscriptionPlanResponse(
    String code,
    String name,
    String description,
    BigDecimal monthlyPrice,
    BigDecimal yearlyPrice,
    String currency,
    Map<String, String> features
) {}
