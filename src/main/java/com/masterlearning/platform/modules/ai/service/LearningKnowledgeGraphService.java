package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearningKnowledgeGraph;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LearningKnowledgeGraphService {
    private final LearningKnowledgeGraphRepository graphRepository;
    private final EnrollmentRepository enrollmentRepository;

    public LearningKnowledgeGraphService(LearningKnowledgeGraphRepository graphRepository,
                                         EnrollmentRepository enrollmentRepository) {
        this.graphRepository = graphRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public LearningKnowledgeGraph forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another user's learning data");
        }

        UUID courseId = enrollment.getCourse().getId();
        List<LearningKnowledgeGraphRepository.NodeRow> nodeRows = graphRepository.findCourseNodes(courseId);
        Map<UUID, LearningKnowledgeGraphRepository.MasteryRow> mastery =
                graphRepository.findLearnerMastery(courseId, userId).stream()
                        .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, Function.identity()));

        List<LearningKnowledgeGraph.Node> nodes = nodeRows.stream()
                .map(n -> {
                    var m = mastery.get(n.id());
                    return new LearningKnowledgeGraph.Node(n.id(), n.name(), n.type(),
                            m == null ? 0.0 : m.mastery(), m == null ? 0L : m.evidenceCount());
                }).toList();

        List<LearningKnowledgeGraph.Edge> edges = graphRepository.findCourseEdges(courseId).stream()
                .filter(e -> nodeRows.stream().anyMatch(n -> n.id().equals(e.sourceId()))
                        && nodeRows.stream().anyMatch(n -> n.id().equals(e.targetId())))
                .map(e -> new LearningKnowledgeGraph.Edge(e.sourceId(), e.targetId(), e.relationType(), e.weight()))
                .toList();

        return new LearningKnowledgeGraph(enrollmentId, courseId, nodes, edges);
    }
}
