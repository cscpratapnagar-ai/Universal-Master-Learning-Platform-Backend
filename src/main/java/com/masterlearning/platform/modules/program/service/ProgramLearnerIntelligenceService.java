package com.masterlearning.platform.modules.program.service;

import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.program.entity.ProgramEnrollment;
import com.masterlearning.platform.modules.program.entity.ProgramMilestoneStatus;
import com.masterlearning.platform.modules.program.repository.ProgramActivityRepository;
import com.masterlearning.platform.modules.program.repository.ProgramEnrollmentRepository;
import com.masterlearning.platform.modules.program.repository.ProgramMilestoneRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class ProgramLearnerIntelligenceService {
    private final ProgramEnrollmentRepository enrollments;
    private final ProgramMilestoneRepository milestones;
    private final ProgramActivityRepository activities;
    private final EnrollmentRepository courseEnrollments;

    public ProgramLearnerIntelligenceService(ProgramEnrollmentRepository enrollments,
                                             ProgramMilestoneRepository milestones,
                                             ProgramActivityRepository activities,
                                             EnrollmentRepository courseEnrollments) {
        this.enrollments = enrollments;
        this.milestones = milestones;
        this.activities = activities;
        this.courseEnrollments = courseEnrollments;
    }

    @Transactional(readOnly = true)
    public Map<String,Object> analyze(UUID programId) {
        List<ProgramEnrollment> list = enrollments.findByProgramIdOrderByCreatedAtAsc(programId);
        LocalDate today = LocalDate.now();
        List<ProgramRisk> risks = new ArrayList<>();

        for (ProgramEnrollment enrollment : list) {
            if ("CANCELLED".equals(enrollment.getStatus())) continue;

            long overdue = milestones.findByProgramIdOrderBySortOrderAscDueDateAsc(programId).stream()
                    .filter(m -> m.getDueDate() != null && m.getDueDate().isBefore(today)
                            && m.getStatus() != ProgramMilestoneStatus.COMPLETED
                            && m.getStatus() != ProgramMilestoneStatus.CANCELLED)
                    .count();

            Instant lastActivity = activities.findByProgramIdAndActorOrderByCreatedAtDesc(
                    programId, enrollment.getUser().getId().toString(), PageRequest.of(0,1))
                    .stream().findFirst().map(a -> a.getCreatedAt()).orElse(enrollment.getCreatedAt());

            long inactiveDays = lastActivity == null ? 0 : Duration.between(lastActivity, Instant.now()).toDays();
            long enrollmentAgeDays = enrollment.getCreatedAt() == null ? 0 : Duration.between(enrollment.getCreatedAt(), Instant.now()).toDays();

            String risk = "ON_TRACK";
            List<String> reasons = new ArrayList<>();

            if (overdue >= 2) {
                risk = "CRITICAL";
                reasons.add("Multiple project milestones are overdue");
            }
            if (inactiveDays >= 14) {
                risk = highest(risk, "CRITICAL");
                reasons.add("No recorded project learning activity for 14+ days");
            } else if (inactiveDays >= 7) {
                risk = highest(risk, "AT_RISK");
                reasons.add("No recorded project learning activity for 7+ days");
            }

            if (enrollment.getProgressPercent() == 0 && enrollmentAgeDays >= 7) {
                risk = highest(risk, "AT_RISK");
                reasons.add("Project has not started");
            } else if (enrollment.getProgressPercent() < 25 && enrollmentAgeDays >= 14) {
                risk = highest(risk, "AT_RISK");
                reasons.add("Project progress remains below 25%");
            }

            if (overdue == 1) {
                risk = highest(risk, "WATCH");
                reasons.add("A project milestone is overdue");
            }

            risks.add(new ProgramRisk(enrollment, risk, reasons, overdue, inactiveDays, lastActivity));
        }

        long critical = risks.stream().filter(r -> r.level().equals("CRITICAL")).count();
        long atRisk = risks.stream().filter(r -> r.level().equals("AT_RISK")).count();
        long watch = risks.stream().filter(r -> r.level().equals("WATCH")).count();
        long onTrack = risks.stream().filter(r -> r.level().equals("ON_TRACK")).count();

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("learnerCount", risks.size());
        out.put("criticalCount", critical);
        out.put("atRiskCount", atRisk);
        out.put("watchCount", watch);
        out.put("onTrackCount", onTrack);
        out.put("learners", risks.stream()
                .sorted(Comparator.comparingInt((ProgramRisk r) -> severity(r.level())).reversed()
                        .thenComparing(ProgramRisk::inactiveDays, Comparator.reverseOrder()))
                .map(this::toMap).toList());
        out.put("generatedAt", Instant.now());
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String,Object> interventionQueue(UUID programId) {
        Map<String,Object> analysis = analyze(programId);
        List<?> learners = (List<?>) analysis.get("learners");
        List<Map<String,Object>> queue = new ArrayList<>();
        for (Object raw : learners) {
            @SuppressWarnings("unchecked") Map<String,Object> learner = (Map<String,Object>) raw;
            String level = String.valueOf(learner.get("riskLevel"));
            if (!"ON_TRACK".equals(level)) {
                Map<String,Object> item = new LinkedHashMap<>(learner);
                item.put("priority", "CRITICAL".equals(level) ? 1 : "AT_RISK".equals(level) ? 2 : 3);
                item.put("interventionType", interventionType(level, (List<String>) learner.get("riskReasons")));
                queue.add(item);
            }
        }
        queue.sort(Comparator.comparingInt(x -> (Integer)x.get("priority")));
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("total", queue.size());
        out.put("immediate", queue.stream().filter(x -> ((Integer)x.get("priority")) == 1).count());
        out.put("targeted", queue.stream().filter(x -> ((Integer)x.get("priority")) == 2).count());
        out.put("watch", queue.stream().filter(x -> ((Integer)x.get("priority")) == 3).count());
        out.put("queue", queue);
        out.put("generatedAt", Instant.now());
        return out;
    }

    private String interventionType(String level, List<String> reasons) {
        String joined = String.join(" ", reasons).toLowerCase();
        if (joined.contains("activity")) return "RE_ENGAGEMENT";
        if (joined.contains("overdue")) return "DEADLINE_SUPPORT";
        if (joined.contains("not started")) return "START_NUDGE";
        if (joined.contains("below 25%")) return "PROGRESS_COACHING";
        return "LEARNER_SUPPORT";
    }

    private int severity(String level) {
        return switch (level) {
            case "CRITICAL" -> 4;
            case "AT_RISK" -> 3;
            case "WATCH" -> 2;
            default -> 1;
        };
    }

    private String highest(String current, String candidate) {
        return severity(candidate) > severity(current) ? candidate : current;
    }

    private Map<String,Object> toMap(ProgramRisk r) {
        Map<String,Object> x = new LinkedHashMap<>();
        x.put("userId", r.enrollment().getUser().getId());
        x.put("userEmail", r.enrollment().getUser().getEmail());
        x.put("userFirstName", r.enrollment().getUser().getFirstName());
        x.put("userLastName", r.enrollment().getUser().getLastName());
        x.put("progressPercent", r.enrollment().getProgressPercent());
        x.put("status", r.enrollment().getStatus());
        x.put("riskLevel", r.level());
        x.put("riskReasons", r.reasons());
        x.put("overdueMilestoneCount", r.overdueMilestones());
        x.put("inactiveDays", r.inactiveDays());
        x.put("lastActivityAt", r.lastActivity());
        x.put("recommendedAction", recommendation(r.level(), r.reasons()));
        return x;
    }

    private String recommendation(String level, List<String> reasons) {
        if ("CRITICAL".equals(level)) return "Immediate learner intervention recommended";
        if ("AT_RISK".equals(level)) return "Review learner progress and send a targeted nudge";
        if ("WATCH".equals(level)) return "Keep under observation and encourage the next learning action";
        return "Learner is currently on track";
    }

    private record ProgramRisk(ProgramEnrollment enrollment, String level, List<String> reasons,
                               long overdueMilestones, long inactiveDays, Instant lastActivity) {}
}
