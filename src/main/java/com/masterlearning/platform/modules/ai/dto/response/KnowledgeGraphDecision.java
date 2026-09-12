package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record KnowledgeGraphDecision(
        UUID enrollmentId,
        UUID courseId,
        String decision,
        UUID targetConceptId,
        String targetConceptName,
        int graphDepth,
        int dependentCount,
        double mastery,
        List<String> reasons
) {}
