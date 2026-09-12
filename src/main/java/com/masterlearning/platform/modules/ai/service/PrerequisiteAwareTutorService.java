package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.LearningConceptMappingRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PrerequisiteAwareTutorService {
    private static final double PREREQUISITE_MASTERY_THRESHOLD = 60.0;
    private static final int MAX_MISSING_PREREQUISITES = 5;

    private final LearningConceptMappingRepository mappings;
    private final ConceptMasteryService conceptMastery;
    private final EnrollmentRepository enrollments;

    public PrerequisiteAwareTutorService(LearningConceptMappingRepository mappings,
                                         ConceptMasteryService conceptMastery,
                                         EnrollmentRepository enrollments) {
        this.mappings = mappings;
        this.conceptMastery = conceptMastery;
        this.enrollments = enrollments;
    }

    public Result analyzeForEnrollment(UUID enrollmentId, List<UUID> targetConceptIds) {
        Enrollment enrollment = enrollments.findById(enrollmentId).orElseThrow();
        return analyze(enrollment.getCourse().getId(), enrollment.getUser().getId(), targetConceptIds);
    }

    public Result analyze(UUID courseId, UUID userId, List<UUID> targetConceptIds) {
        if (courseId == null || userId == null || targetConceptIds == null || targetConceptIds.isEmpty()) {
            return new Result(List.of(), false);
        }

        Map<UUID, Double> masteryByConcept = new LinkedHashMap<>();
        conceptMastery.forLearner(courseId, userId)
                .forEach(m -> masteryByConcept.put(m.conceptId(), m.mastery()));

        Set<UUID> visited = new HashSet<>();
        Set<UUID> missing = new LinkedHashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>(targetConceptIds);

        while (!queue.isEmpty() && missing.size() < MAX_MISSING_PREREQUISITES) {
            UUID conceptId = queue.removeFirst();
            if (!visited.add(conceptId)) continue;
            List<UUID> prerequisites = mappings.findDirectPrerequisiteIds(List.of(conceptId))
                    .getOrDefault(conceptId, List.of());
            for (UUID prerequisiteId : prerequisites) {
                double mastery = masteryByConcept.getOrDefault(prerequisiteId, 0.0);
                if (mastery < PREREQUISITE_MASTERY_THRESHOLD) {
                    missing.add(prerequisiteId);
                    if (missing.size() >= MAX_MISSING_PREREQUISITES) break;
                }
                queue.addLast(prerequisiteId);
            }
        }

        Map<UUID, String> names = missing.isEmpty()
                ? Map.of()
                : mappings.findActiveConceptNames(new ArrayList<>(missing));
        List<MissingPrerequisite> result = missing.stream()
                .map(id -> new MissingPrerequisite(id, names.getOrDefault(id, "Unknown prerequisite"),
                        Math.round(masteryByConcept.getOrDefault(id, 0.0) * 100.0) / 100.0))
                .toList();
        return new Result(result, !result.isEmpty());
    }

    public record MissingPrerequisite(UUID conceptId, String name, double mastery) {}
    public record Result(List<MissingPrerequisite> missingPrerequisites, boolean prerequisiteBlocker) {}
}
