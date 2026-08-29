package com.masterlearning.platform.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        boolean success,
        String code,
        String message,
        Map<String, String> errors,
        Instant timestamp
) {
    public static ApiErrorResponse of(
            String code,
            String message,
            Map<String, String> errors
    ) {
        return new ApiErrorResponse(
                false,
                code,
                message,
                errors == null ? Map.of() : Map.copyOf(errors),
                Instant.now()
        );
    }
}