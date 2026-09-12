package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record LearningKnowledgeGraph(
        UUID enrollmentId,
        UUID courseId,
        List<Node> nodes,
        List<Edge> edges
) {
    public record Node(UUID id, String name, String type, double mastery, long evidenceCount) {}
    public record Edge(UUID sourceId, UUID targetId, String relationType, double weight) {}
}
