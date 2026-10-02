package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.response.StudentQuestionResponse;
import com.masterlearning.platform.modules.assessment.repository.*;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LessonPrerequisiteRepository;
import com.masterlearning.platform.modules.course.repository.LessonProgressRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/student/assessments")
public class StudentAssessmentQuestionsController {
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final EnrollmentRepository enrollments;
    private final LessonPrerequisiteRepository prerequisites;
    private final LessonProgressRepository progress;

    public StudentAssessmentQuestionsController(
            AssessmentRepository assessments,
            QuestionRepository questions,
            QuestionOptionRepository options,
            EnrollmentRepository enrollments,
            LessonPrerequisiteRepository prerequisites,
            LessonProgressRepository progress) {
        this.assessments = assessments;
        this.questions = questions;
        this.options = options;
        this.enrollments = enrollments;
        this.prerequisites = prerequisites;
        this.progress = progress;
    }

    @GetMapping("/{assessmentId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StudentQuestionResponse>> questions(@PathVariable UUID assessmentId) {
        var assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));

        UUID userId = SecurityUtils.getCurrentUserId();
        var enrollment = enrollments.findByCourseIdAndUserId(
                assessment.getCourse().getId(), userId)
                .orElseThrow(() -> new AccessDeniedException("You are not enrolled in this course"));

        if (assessment.getLesson() != null) {
            var unmet = prerequisites.findByIdLessonId(assessment.getLesson().getId()).stream()
                    .map(p -> p.getPrerequisiteLessonId())
                    .filter(id -> !progress.findByEnrollmentIdAndLessonId(enrollment.getId(), id)
                            .map(com.masterlearning.platform.modules.course.entity.LessonProgress::isCompleted)
                            .orElse(false))
                    .toList();
            if (!unmet.isEmpty()) {
                throw new AccessDeniedException(
                        "Lesson assessment is locked until all prerequisites are completed");
            }
        }

        var data = questions.findByAssessmentId(assessmentId).stream()
                .map(q -> new StudentQuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getQuestionType(),
                        q.getPoints(),
                        options.findByQuestionId(q.getId()).stream()
                                .map(o -> new StudentQuestionResponse.Option(o.getId(), o.getOptionText()))
                                .toList()))
                .toList();

        return ApiResponse.success("Assessment questions retrieved", data);
    }
}
