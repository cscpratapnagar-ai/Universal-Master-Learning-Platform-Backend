package com.masterlearning.platform.billing;

public record VerifyRazorpayPaymentRequest(
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature
) {}
