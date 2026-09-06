package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record GroundedTutorResponse(
        UUID enrollmentId,
        String response,
        boolean grounded,
        boolean llmUsed,
        List<Source> sources
) {
    public record Source(UUID lessonId, String lessonTitle, double relevance) {}
}
