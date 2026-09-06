package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.AdaptiveNextActionResponse;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.entity.LessonPrerequisite;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AdaptiveDecisionEngine {

    private static final double PREREQUISITE_PRIORITY = 0.95;
    private static final double NEXT_LESSON_PRIORITY = 0.80;

    public AdaptiveNextActionResponse decide(
            List<Lesson> orderedLessons,
            List<LessonPrerequisite> prerequisiteLinks,
            Set<UUID> completedLessonIds,
            double masteryScore) {

        if (orderedLessons == null || orderedLessons.isEmpty()) {
            return new AdaptiveNextActionResponse(
                    "NONE", null, null, "COURSE_COMPLETE", 0.0,
                    normalizedMastery(masteryScore), false, null);
        }

        Map<UUID, List<UUID>> prerequisitesByLesson = prerequisiteLinks == null
                ? Map.of()
                : prerequisiteLinks.stream().collect(Collectors.groupingBy(
                        LessonPrerequisite::getLessonId,
                        LinkedHashMap::new,
                        Collectors.mapping(LessonPrerequisite::getPrerequisiteLessonId, Collectors.toList())));

        Set<UUID> completed = completedLessonIds == null ? Set.of() : completedLessonIds;

        for (Lesson lesson : orderedLessons) {
            if (completed.contains(lesson.getId())) continue;

            List<UUID> blockers = prerequisitesByLesson.getOrDefault(lesson.getId(), List.of())
                    .stream()
                    .filter(id -> !completed.contains(id))
                    .toList();

            if (!blockers.isEmpty()) {
                UUID prerequisiteId = firstExistingLessonId(orderedLessons, blockers);
                Lesson prerequisite = orderedLessons.stream()
                        .filter(l -> l.getId().equals(prerequisiteId))
                        .findFirst()
                        .orElse(null);
                return new AdaptiveNextActionResponse(
                        "LESSON",
                        prerequisite == null ? prerequisiteId : prerequisite.getId(),
                        prerequisite == null ? null : prerequisite.getTitle(),
                        "PREREQUISITE_GAP",
                        PREREQUISITE_PRIORITY,
                        normalizedMastery(masteryScore),
                        true,
                        prerequisiteId);
            }

            return new AdaptiveNextActionResponse(
                    "LESSON",
                    lesson.getId(),
                    lesson.getTitle(),
                    masteryScore < 50 ? "MASTERY_GAP" : "NEXT_IN_SEQUENCE",
                    masteryScore < 50 ? 0.90 : NEXT_LESSON_PRIORITY,
                    normalizedMastery(masteryScore),
                    false,
                    null);
        }

        return new AdaptiveNextActionResponse(
                "NONE", null, null, "COURSE_COMPLETE", 0.0,
                normalizedMastery(masteryScore), false, null);
    }

    private UUID firstExistingLessonId(List<Lesson> lessons, List<UUID> candidateIds) {
        for (Lesson lesson : lessons) {
            if (candidateIds.contains(lesson.getId())) return lesson.getId();
        }
        return candidateIds.get(0);
    }

    private double normalizedMastery(double masteryScore) {
        if (Double.isNaN(masteryScore) || Double.isInfinite(masteryScore)) return 0.0;
        return Math.max(0.0, Math.min(100.0, Math.round(masteryScore * 10.0) / 10.0));
    }
}
