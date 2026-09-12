package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.KnowledgeGraphDecision;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphDecisionService {
    private static final double WEAK = 60.0;
    private final LearningKnowledgeGraphRepository graph;
    private final EnrollmentRepository enrollments;

    public KnowledgeGraphDecisionService(LearningKnowledgeGraphRepository graph, EnrollmentRepository enrollments) {
        this.graph = graph;
        this.enrollments = enrollments;
    }

    public KnowledgeGraphDecision decide(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another user's learning data");
        }
        UUID courseId = enrollment.getCourse().getId();
        var nodes = graph.findCourseNodes(courseId);
        var mastery = graph.findLearnerMastery(courseId, userId).stream()
                .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, Function.identity()));
        var edges = graph.findCourseEdges(courseId);
        Map<UUID, String> names = nodes.stream().collect(Collectors.toMap(LearningKnowledgeGraphRepository.NodeRow::id,
                LearningKnowledgeGraphRepository.NodeRow::name));
        Map<UUID, List<UUID>> dependents = new HashMap<>();
        for (var edge : edges) dependents.computeIfAbsent(edge.targetId(), k -> new ArrayList<>()).add(edge.sourceId());

        var candidate = nodes.stream()
                .filter(n -> mastery.getOrDefault(n.id(), new LearningKnowledgeGraphRepository.MasteryRow(n.id(), 0, 0)).mastery() < WEAK)
                .map(n -> new Candidate(n.id(), n.name(), mastery.getOrDefault(n.id(), new LearningKnowledgeGraphRepository.MasteryRow(n.id(), 0, 0)).mastery(), dependentDepth(n.id(), dependents)))
                .max(Comparator.comparingInt((Candidate c) -> c.dependents).thenComparingDouble(c -> -c.mastery))
                .orElse(null);

        if (candidate == null) {
            return new KnowledgeGraphDecision(enrollmentId, courseId, "ADVANCE", null, null, 0, 0, 100,
                    List.of("No weak concept is blocking the current learning graph"));
        }
        String decision = candidate.mastery < 40 ? "REMEDIATE" : "TARGETED_PRACTICE";
        List<String> reasons = new ArrayList<>();
        reasons.add(candidate.mastery < 40 ? "Critical mastery gap" : "Concept is below mastery threshold");
        if (candidate.dependents > 0) reasons.add("Improving this node unlocks downstream concepts");
        return new KnowledgeGraphDecision(enrollmentId, courseId, decision, candidate.id, candidate.name,
                candidate.depth, candidate.dependents, candidate.mastery, List.copyOf(reasons));
    }

    private int dependentDepth(UUID root, Map<UUID, List<UUID>> dependents) {
        Queue<Map.Entry<UUID,Integer>> queue = new ArrayDeque<>();
        Set<UUID> seen = new HashSet<>();
        queue.add(Map.entry(root, 0));
        int max = 0;
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (!seen.add(current.getKey())) continue;
            max = Math.max(max, current.getValue());
            for (UUID next : dependents.getOrDefault(current.getKey(), List.of())) {
                queue.add(Map.entry(next, current.getValue() + 1));
            }
        }
        return max;
    }

    private record Candidate(UUID id, String name, double mastery, int dependents) {
        int depth() { return dependents; }
    }
}
