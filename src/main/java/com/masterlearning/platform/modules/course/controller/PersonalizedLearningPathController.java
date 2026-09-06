package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.course.dto.response.PersonalizedLearningPathResponse;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LessonPrerequisiteRepository;
import com.masterlearning.platform.modules.course.repository.LessonProgressRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.course.service.PersonalizedLearningPathEngine;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/student/learning")
public class PersonalizedLearningPathController {
    private final EnrollmentRepository enrollments;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final LessonPrerequisiteRepository prerequisites;
    private final AssessmentAttemptRepository attempts;
    private final PersonalizedLearningPathEngine engine;

    public PersonalizedLearningPathController(EnrollmentRepository enrollments, CourseModuleRepository modules,
            LessonRepository lessons, LessonProgressRepository progress, LessonPrerequisiteRepository prerequisites,
            AssessmentAttemptRepository attempts, PersonalizedLearningPathEngine engine) {
        this.enrollments = enrollments; this.modules = modules; this.lessons = lessons; this.progress = progress;
        this.prerequisites = prerequisites; this.attempts = attempts; this.engine = engine;
    }

    @GetMapping("/enrollments/{enrollmentId}/personalized-path")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<PersonalizedLearningPathResponse> personalizedPath(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId))
            throw new AccessDeniedException("You cannot access another user's personalized path");
        UUID courseId = enrollment.getCourse().getId();
        List<Lesson> ordered = modules.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .flatMap(m -> lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).stream()).toList();
        Set<UUID> completed = new HashSet<>();
        for (Lesson lesson : ordered)
            progress.findByEnrollmentIdAndLessonId(enrollmentId, lesson.getId())
                    .filter(p -> p.isCompleted()).ifPresent(p -> completed.add(lesson.getId()));
        List<AssessmentAttempt> passed = attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(courseId, userId).stream()
                .filter(AssessmentAttempt::isPassed).toList();
        double mastery = passed.stream().mapToInt(AssessmentAttempt::getScore).average().orElse(0.0);
        List<com.masterlearning.platform.modules.course.entity.LessonPrerequisite> links = new ArrayList<>();
        for (Lesson lesson : ordered) links.addAll(prerequisites.findByIdLessonId(lesson.getId()));
        return ApiResponse.success("Personalized learning path generated", engine.build(enrollmentId, ordered, links, completed, mastery));
    }
}
