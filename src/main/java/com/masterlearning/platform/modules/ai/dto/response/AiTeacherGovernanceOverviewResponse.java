package com.masterlearning.platform.modules.ai.dto.response;

public record AiTeacherGovernanceOverviewResponse(
        long activeAiTeacherUsers,
        long monthlyTurns,
        long averageTurnsPerUser,
        String periodStart
) {}
