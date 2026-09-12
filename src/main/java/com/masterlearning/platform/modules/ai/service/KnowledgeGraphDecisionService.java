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
        Map<UUID, List<UUID>> dependents = new HashMap<>();
        for (var edge : edges) dependents.computeIfAbsent(edge.targetId(), k -> new ArrayList<>()).add(edge.sourceId());

        Candidate candidate = nodes.stream()
                .filter(n -> mastery.getOrDefault(n.id(), new LearningKnowledgeGraphRepository.MasteryRow(n.id(), 0, 0)).mastery() < WEAK)
                .map(n -> {
                    var m = mastery.getOrDefault(n.id(), new LearningKnowledgeGraphRepository.MasteryRow(n.id(), 0, 0));
                    Impact impact = impact(n.id(), dependents);
                    return new Candidate(n.id(), n.name(), m.mastery(), impact.count(), impact.depth());
                })
                .max(Comparator.comparingInt(Candidate::dependentCount)
                        .thenComparingInt(Candidate::depth)
                        .thenComparingDouble(c -> -c.mastery()))
                .orElse(null);

        if (candidate == null) {
            return new KnowledgeGraphDecision(enrollmentId, courseId, "ADVANCE", null, null, 0, 0, 100,
                    List.of("No weak concept is blocking the current learning graph"));
        }
        String decision = candidate.mastery() < 40 ? "REMEDIATE" : "TARGETED_PRACTICE";
        List<String> reasons = new ArrayList<>();
        reasons.add(candidate.mastery() < 40 ? "Critical mastery gap" : "Concept is below mastery threshold");
        if (candidate.dependentCount() > 0) reasons.add("Improving this node unlocks downstream concepts");
        return new KnowledgeGraphDecision(enrollmentId, courseId, decision, candidate.id(), candidate.name(),
                candidate.depth(), candidate.dependentCount(), candidate.mastery(), List.copyOf(reasons));
    }

    private Impact impact(UUID root, Map<UUID, List<UUID>> dependents) {
        Queue<Map.Entry<UUID,Integer>> queue = new ArrayDeque<>();
        Set<UUID> seen = new HashSet<>();
        queue.add(Map.entry(root, 0));
        int maxDepth = 0;
        int count = 0;
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (!seen.add(current.getKey())) continue;
            maxDepth = Math.max(maxDepth, current.getValue());
            if (current.getValue() > 0) count++;
            for (UUID next : dependents.getOrDefault(current.getKey(), List.of())) {
                queue.add(Map.entry(next, current.getValue() + 1));
            }
        }
        return new Impact(count, maxDepth);
    }

    private record Impact(int count, int depth) {}
    private record Candidate(UUID id, String name, double mastery, int dependentCount, int depth) {}
}
