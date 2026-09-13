package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record AiKnowledgeContext(
        UUID enrollmentId,
        UUID courseId,
        List<KnowledgeNode> relevantNodes,
        List<KnowledgeEdge> relevantEdges,
        List<String> weakConcepts,
        List<String> prerequisiteConcepts,
        List<String> groundedSources
) {
    public record KnowledgeNode(UUID id, String name, String type, double mastery, long evidenceCount) {}
    public record KnowledgeEdge(UUID sourceId, UUID targetId, String relationType, double weight) {}
}
