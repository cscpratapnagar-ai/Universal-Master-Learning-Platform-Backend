package com.masterlearning.platform.modules.ai.dto.response;

public record UnderstandingEvaluation(
        String signal,
        String nextAction,
        boolean needsRetry,
        boolean needsPractice
) {}
