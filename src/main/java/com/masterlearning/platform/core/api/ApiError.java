package com.masterlearning.platform.core.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    boolean success,
    String code,
    String message,
    Map<String, String> fields,
    Instant timestamp
) {
    public static ApiError of(String code, String message, Map<String, String> fields) {
        return new ApiError(false, code, message, fields == null ? Map.of() : fields, Instant.now());
    }
}