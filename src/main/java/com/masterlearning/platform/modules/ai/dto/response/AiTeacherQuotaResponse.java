package com.masterlearning.platform.modules.ai.dto.response;

import java.time.LocalDate;

public record AiTeacherQuotaResponse(
        String plan,
        int used,
        int limit,
        int remaining,
        LocalDate periodStart
) {
    public static AiTeacherQuotaResponse unlimited(String plan, int used, LocalDate periodStart) {
        return new AiTeacherQuotaResponse(plan, used, -1, -1, periodStart);
    }
}
