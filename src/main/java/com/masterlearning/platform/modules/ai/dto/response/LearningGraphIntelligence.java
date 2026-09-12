package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record LearningGraphIntelligence(
        UUID enrollmentId,
        UUID courseId,
        List<WeakNode> weakNodes,
        List<ImpactNode> impactedNodes,
        List<Recommendation> recommendations
) {
    public record WeakNode(UUID conceptId, String name, double mastery, long evidenceCount, String severity) {}
    public record ImpactNode(UUID conceptId, String name, double mastery, int dependencyDistance, String reason) {}
    public record Recommendation(UUID conceptId, String name, String action, int priority, String reason) {}
}
