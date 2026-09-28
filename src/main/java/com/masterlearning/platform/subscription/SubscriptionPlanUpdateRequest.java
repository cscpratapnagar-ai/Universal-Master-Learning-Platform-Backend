package com.masterlearning.platform.subscription;

import java.math.BigDecimal;
import java.util.Map;

public record SubscriptionPlanUpdateRequest(
    String name,
    String description,
    BigDecimal monthlyPrice,
    BigDecimal yearlyPrice,
    String currency,
    Boolean active,
    Map<String, String> features
) {}