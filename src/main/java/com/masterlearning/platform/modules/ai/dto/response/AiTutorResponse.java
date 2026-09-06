package com.masterlearning.platform.modules.ai.dto.response;

import java.util.UUID;

public record AiTutorResponse(
        UUID enrollmentId,
        String intent,
        String learnerState,
        String response,
        String nextAction,
        String rationale,
        boolean llmReady
) {}
