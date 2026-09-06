package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.NextBestLearningResponse;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.entity.LessonPrerequisite;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class NextBestLearningEngine {
    public NextBestLearningResponse recommend(UUID enrollmentId, List<Lesson> lessons,
            List<LessonPrerequisite> links, Set<UUID> completed, Set<UUID> weakLessonIds, double mastery) {
        List<Lesson> ordered = lessons == null ? List.of() : lessons;
        Set<UUID> done = completed == null ? Set.of() : completed;
        Set<UUID> weak = weakLessonIds == null ? Set.of() : weakLessonIds;
        Map<UUID, List<UUID>> deps = links == null ? Map.of() : links.stream().collect(Collectors.groupingBy(
                LessonPrerequisite::getLessonId, LinkedHashMap::new,
                Collectors.mapping(LessonPrerequisite::getPrerequisiteLessonId, Collectors.toList())));

        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            Lesson lesson = ordered.get(i);
            if (done.contains(lesson.getId())) continue;
            List<UUID> blockers = deps.getOrDefault(lesson.getId(), List.of()).stream()
                    .filter(id -> !done.contains(id)).toList();
            if (!blockers.isEmpty()) continue;
            double score;
            String reason;
            String type;
            if (weak.contains(lesson.getId())) {
                score = 1.00; reason = "Remediate a detected knowledge gap"; type = "REMEDIATION";
            } else if (mastery >= 85) {
                score = .90 - (i * .001); reason = "Accelerate because mastery is strong"; type = "ACCELERATION";
            } else if (mastery < 50) {
                score = .88 - (i * .001); reason = "Build mastery before advancing"; type = "MASTERY_BUILDING";
            } else {
                score = .80 - (i * .001); reason = "Continue the optimal course sequence"; type = "SEQUENCE";
            }
            candidates.add(new Candidate(lesson, Math.max(.0, score), reason, type));
        }

        if (candidates.isEmpty()) {
            String state = ordered.isEmpty() || done.size() >= ordered.size() ? "COMPLETE" : "BLOCKED";
            return new NextBestLearningResponse(enrollmentId, state, "NONE", null, null, 0.0,
                    state.equals("COMPLETE") ? "All lessons are completed" : "Prerequisites must be completed first", List.of());
        }

        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed());
        Candidate best = candidates.get(0);
        List<NextBestLearningResponse.Alternative> alternatives = candidates.stream().skip(1).limit(3)
                .map(c -> new NextBestLearningResponse.Alternative(c.lesson().getId(), c.lesson().getTitle(), c.reason(), round(c.score())))
                .toList();
        String state = mastery < 50 ? "REMEDIATION" : mastery >= 85 ? "ACCELERATED" : "STANDARD";
        return new NextBestLearningResponse(enrollmentId, state, best.type(), best.lesson().getId(),
                best.lesson().getTitle(), round(best.score()), best.reason(), alternatives);
    }

    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
    private record Candidate(Lesson lesson, double score, String reason, String type) {}
}
