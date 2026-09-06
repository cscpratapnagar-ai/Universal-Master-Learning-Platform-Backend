package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.PersonalizedLearningPathResponse;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.entity.LessonPrerequisite;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PersonalizedLearningPathEngine {
    public PersonalizedLearningPathResponse build(UUID enrollmentId, List<Lesson> lessons,
            List<LessonPrerequisite> links, Set<UUID> completed, double mastery) {
        List<Lesson> ordered = lessons == null ? List.of() : lessons;
        Set<UUID> done = completed == null ? Set.of() : completed;
        Map<UUID,List<UUID>> deps = links == null ? Map.of() : links.stream().collect(Collectors.groupingBy(
                LessonPrerequisite::getIdLessonId, LinkedHashMap::new,
                Collectors.mapping(LessonPrerequisite::getPrerequisiteLessonId, Collectors.toList())));
        List<PersonalizedLearningPathResponse.PathItem> path = new ArrayList<>();
        int sequence = 1;
        for (Lesson lesson : ordered) {
            if (done.contains(lesson.getId())) continue;
            List<UUID> blockers = deps.getOrDefault(lesson.getId(), List.of()).stream()
                    .filter(id -> !done.contains(id)).toList();
            if (!blockers.isEmpty()) {
                UUID target = ordered.stream().map(Lesson::getId).filter(blockers::contains).findFirst().orElse(blockers.get(0));
                Lesson p = ordered.stream().filter(x -> x.getId().equals(target)).findFirst().orElse(null);
                if (p != null && path.stream().noneMatch(x -> x.lessonId().equals(p.getId())))
                    path.add(new PersonalizedLearningPathResponse.PathItem(p.getId(), p.getTitle(), sequence++, "REMEDIATE_PREREQUISITE", "Unlock prerequisite", .98, true));
                continue;
            }
            String action = mastery < 50 ? "REMEDIATE" : mastery >= 85 ? "ACCELERATE" : "LEARN";
            String reason = mastery < 50 ? "Address mastery gap" : mastery >= 85 ? "Accelerate through strong mastery" : "Continue course sequence";
            double priority = mastery < 50 ? .92 : mastery >= 85 ? .75 : .80;
            path.add(new PersonalizedLearningPathResponse.PathItem(lesson.getId(), lesson.getTitle(), sequence++, action, reason, priority, false));
        }
        String state = ordered.isEmpty() || done.size() >= ordered.size() ? "COMPLETE" : mastery < 50 ? "REMEDIATION" : mastery >= 85 ? "ACCELERATED" : "STANDARD";
        return new PersonalizedLearningPathResponse(enrollmentId, state, normalized(mastery), ordered.size(), done.size(), Math.max(0, ordered.size()-done.size()), path);
    }
    private double normalized(double v) { if (Double.isNaN(v) || Double.isInfinite(v)) return 0; return Math.max(0, Math.min(100, Math.round(v*10)/10.0)); }
}
