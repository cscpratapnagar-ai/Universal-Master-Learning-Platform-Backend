package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AiKnowledgeContext;
import com.masterlearning.platform.modules.ai.dto.response.LearningKnowledgeGraph;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AiKnowledgeContextService {
    private static final double WEAK_THRESHOLD = 60.0;
    private static final int MAX_ITEMS = 5;

    private final EnrollmentRepository enrollments;
    private final LearningKnowledgeGraphService knowledgeGraph;
    private final ConceptMasteryService conceptMastery;

    public AiKnowledgeContextService(EnrollmentRepository enrollments,
                                     LearningKnowledgeGraphService knowledgeGraph,
                                     ConceptMasteryService conceptMastery) {
        this.enrollments = enrollments;
        this.knowledgeGraph = knowledgeGraph;
        this.conceptMastery = conceptMastery;
    }

    public AiKnowledgeContext forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another user's learning data");
        }

        UUID courseId = enrollment.getCourse().getId();
        LearningKnowledgeGraph graph = knowledgeGraph.forEnrollment(enrollmentId, userId);

        List<AiKnowledgeContext.KnowledgeNode> weakNodes = graph.nodes().stream()
                .filter(n -> n.mastery() < WEAK_THRESHOLD)
                .sorted((a, b) -> Double.compare(a.mastery(), b.mastery()))
                .limit(MAX_ITEMS)
                .map(n -> new AiKnowledgeContext.KnowledgeNode(n.id(), n.name(), n.type(), n.mastery(), n.evidenceCount()))
                .toList();

        List<String> weakConcepts = conceptMastery.weakConcepts(courseId, userId, WEAK_THRESHOLD, MAX_ITEMS);

        List<AiKnowledgeContext.KnowledgeEdge> edges = graph.edges().stream()
                .filter(e -> weakNodes.stream().anyMatch(n -> n.id().equals(e.sourceId()) || n.id().equals(e.targetId())))
                .limit(MAX_ITEMS * 2L)
                .map(e -> new AiKnowledgeContext.KnowledgeEdge(e.sourceId(), e.targetId(), e.relationType(), e.weight()))
                .toList();

        List<String> prerequisites = edges.stream()
                .filter(e -> "PREREQUISITE".equalsIgnoreCase(e.relationType()))
                .map(e -> graph.nodes().stream()
                        .filter(n -> n.id().equals(e.targetId()))
                        .map(LearningKnowledgeGraph.Node::name)
                        .findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .limit(MAX_ITEMS)
                .toList();

        List<String> sources = weakNodes.stream()
                .map(AiKnowledgeContext.KnowledgeNode::name)
                .distinct()
                .toList();

        return new AiKnowledgeContext(enrollmentId, courseId, weakNodes, edges,
                weakConcepts, prerequisites, sources);
    }
}
