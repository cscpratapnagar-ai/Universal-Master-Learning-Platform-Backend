package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.request.CreateAssessmentRequest;
import com.masterlearning.platform.modules.assessment.dto.request.CreateQuestionRequest;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.entity.QuestionOption;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionOptionRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import com.masterlearning.platform.modules.course.entity.Course;
import com.masterlearning.platform.modules.course.entity.CourseModule;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;

    public AssessmentController(AssessmentRepository assessments, QuestionRepository questions,
                                QuestionOptionRepository options, CourseRepository courses,
                                CourseModuleRepository modules, LessonRepository lessons) {
        this.assessments = assessments;
        this.questions = questions;
        this.options = options;
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
    }

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    public ApiResponse<Map<String,Object>> createCourseAssessment(@PathVariable UUID courseId,
                                                                    @Valid @RequestBody CreateAssessmentRequest request) {
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        Assessment assessment = assessments.save(new Assessment(course, null, null, "COURSE", request.title(), request.passingScore(), maxAttempts(request)));
        return ApiResponse.success("Course assessment created",
                Map.of("id", assessment.getId(), "title", assessment.getTitle(), "level", assessment.getAssessmentLevel()));
    }

    @PostMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    @Transactional
    public ApiResponse<Map<String,Object>> createModuleAssessment(@PathVariable UUID moduleId,
                                                                    @Valid @RequestBody CreateAssessmentRequest request) {
        CourseModule module = modules.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));
        Assessment assessment = assessments.save(new Assessment(module.getCourse(), module, null,
                "MODULE", request.title(), request.passingScore(), maxAttempts(request)));
        return ApiResponse.success("Module assessment created",
                Map.of("id", assessment.getId(), "title", assessment.getTitle(), "level", assessment.getAssessmentLevel()));
    }

    @PostMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    @Transactional
    public ApiResponse<Map<String,Object>> createLessonAssessment(@PathVariable UUID lessonId,
                                                                    @Valid @RequestBody CreateAssessmentRequest request) {
        Lesson lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
        CourseModule module = lesson.getModule();
        Assessment assessment = assessments.save(new Assessment(module.getCourse(), module, lesson,
                "LESSON", request.title(), request.passingScore(), maxAttempts(request)));
        return ApiResponse.success("Lesson assessment created",
                Map.of("id", assessment.getId(), "title", assessment.getTitle(), "level", assessment.getAssessmentLevel()));
    }

    private int maxAttempts(CreateAssessmentRequest request) { return request.maxAttempts() == null ? 3 : request.maxAttempts(); }

    @PostMapping("/{assessmentId}/questions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    public ApiResponse<Map<String,Object>> question(@PathVariable UUID assessmentId,
                                                     @Valid @RequestBody CreateQuestionRequest request) {
        Assessment assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        if (request.options().stream().noneMatch(CreateQuestionRequest.Option::correct)) {
            throw new IllegalArgumentException("At least one correct option is required");
        }
        Question question = questions.save(new Question(assessment, request.questionText(),
                request.questionType() == null ? "SINGLE_CHOICE" : request.questionType(), request.points()));
        request.options().forEach(option -> options.save(new QuestionOption(question, option.text(), option.correct())));
        return ApiResponse.success("Question created", Map.of("id", question.getId()));
    }
}
