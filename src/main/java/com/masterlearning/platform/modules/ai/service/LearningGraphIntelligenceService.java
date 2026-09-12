package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearningGraphIntelligence;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LearningGraphIntelligenceService {
    private static final double WEAK_THRESHOLD = 60.0;
    private static final double CRITICAL_THRESHOLD = 40.0;
    private final LearningKnowledgeGraphRepository graphRepository;
    private final EnrollmentRepository enrollmentRepository;

    public LearningGraphIntelligenceService(LearningKnowledgeGraphRepository graphRepository,
                                             EnrollmentRepository enrollmentRepository) {
        this.graphRepository = graphRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public LearningGraphIntelligence forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another user's learning data");
        }
        UUID courseId = enrollment.getCourse().getId();
        var nodes = graphRepository.findCourseNodes(courseId);
        var mastery = graphRepository.findLearnerMastery(courseId, userId).stream()
                .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, Function.identity()));
        var edges = graphRepository.findCourseEdges(courseId);
        Map<UUID, String> names = nodes.stream().collect(Collectors.toMap(LearningKnowledgeGraphRepository.NodeRow::id,
                LearningKnowledgeGraphRepository.NodeRow::name));

        List<LearningGraphIntelligence.WeakNode> weak = nodes.stream()
                .map(n -> Map.entry(n, mastery.getOrDefault(n.id(), new LearningKnowledgeGraphRepository.MasteryRow(n.id(), 0, 0))))
                .filter(x -> x.getValue().mastery() < WEAK_THRESHOLD)
                .sorted(Comparator.comparingDouble(x -> x.getValue().mastery()))
                .map(x -> new LearningGraphIntelligence.WeakNode(x.getKey().id(), x.getKey().name(),
                        x.getValue().mastery(), x.getValue().evidenceCount(),
                        x.getValue().mastery() < CRITICAL_THRESHOLD ? "CRITICAL" : "WEAK"))
                .toList();

        Map<UUID, List<UUID>> dependents = new HashMap<>();
        for (var e : edges) dependents.computeIfAbsent(e.targetId(), k -> new ArrayList<>()).add(e.sourceId());
        List<LearningGraphIntelligence.ImpactNode> impacted = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (var w : weak) {
            Queue<Map.Entry<UUID,Integer>> q = new ArrayDeque<>();
            q.add(Map.entry(w.conceptId(), 0));
            Set<UUID> local = new HashSet<>();
            while (!q.isEmpty()) {
                var cur = q.remove();
                if (!local.add(cur.getKey())) continue;
                if (cur.getValue() > 0 && seen.add(cur.getKey()) && names.containsKey(cur.getKey())) {
                    var m = mastery.get(cur.getKey());
                    impacted.add(new LearningGraphIntelligence.ImpactNode(cur.getKey(), names.get(cur.getKey()),
                            m == null ? 0 : m.mastery(), cur.getValue(), "Depends on weak concept " + w.name()));
                }
                if (cur.getValue() < 3) {
                    for (UUID next : dependents.getOrDefault(cur.getKey(), List.of())) q.add(Map.entry(next, cur.getValue() + 1));
                }
            }
        }

        List<LearningGraphIntelligence.Recommendation> recommendations = weak.stream()
                .limit(5)
                .map(w -> new LearningGraphIntelligence.Recommendation(w.conceptId(), w.name(),
                        w.mastery() < CRITICAL_THRESHOLD ? "REMEDIATE_FIRST" : "TARGETED_PRACTICE",
                        w.mastery() < CRITICAL_THRESHOLD ? 1 : 2,
                        w.mastery() < CRITICAL_THRESHOLD ? "Critical mastery gap" : "Weak concept requires reinforcement"))
                .toList();

        return new LearningGraphIntelligence(enrollmentId, courseId, weak, impacted, recommendations);
    }
}
