package com.masterlearning.platform.billing;

import java.math.BigDecimal;

public record RefundRequest(BigDecimal amount, String reason) {}
