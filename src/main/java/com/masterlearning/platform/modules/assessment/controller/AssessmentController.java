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
import com.masterlearning.platform.modules.course.security.CourseAuthorizationService;
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
    private final CourseAuthorizationService authorization;

    public AssessmentController(AssessmentRepository assessments, QuestionRepository questions,
                                QuestionOptionRepository options, CourseRepository courses,
                                CourseModuleRepository modules, LessonRepository lessons) {
        this.assessments = assessments;
        this.questions = questions;
        this.options = options;
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
        this.authorization = authorization;
    }

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional
    public ApiResponse<Map<String,Object>> createCourseAssessment(@PathVariable UUID courseId,
                                                                    @Valid @RequestBody CreateAssessmentRequest request) {
        Course course = courses.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found"));
        authorization.assertCanManage(course);
        Assessment assessment = assessments.saveAndFlush(new Assessment(course, null, null, "COURSE", request.title(), request.passingScore(), maxAttempts(request)));
        return ApiResponse.success("Course assessment created", Map.of("id", assessment.getId(), "title", assessment.getTitle(), "level", assessment.getAssessmentLevel()));
    }

    @PostMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional
    public ApiResponse<Map<String,Object>> createModuleAssessment(@PathVariable UUID moduleId,
                                                                    @Valid @RequestBody CreateAssessmentRequest request) {
        CourseModule module = modules.findById(moduleId).orElseThrow(() -> new EntityNotFoundException("Module not found"));
        authorization.assertCanManage(module.getCourse());
        Assessment assessment = assessments.saveAndFlush(new Assessment(module.getCourse(), module, null, "MODULE", request.title(), request.passingScore(), maxAttempts(request)));
        return ApiResponse.success("Module assessment created", Map.of("id", assessment.getId(), "title", assessment.getTitle(), "level", assessment.getAssessmentLevel()));
    }

    @PostMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional
    public ApiResponse<Map<String,Object>> createLessonAssessment(@PathVariable UUID lessonId,
                                                                    @Valid @RequestBody CreateAssessmentRequest request) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
        authorization.assertCanManage(lesson.getModule().getCourse());
        lesson.setCompletionMode("ASSESSMENT_REQUIRED");
        lessons.saveAndFlush(lesson);
        CourseModule module = lesson.getModule();
        Assessment assessment = assessments.saveAndFlush(new Assessment(module.getCourse(), module, lesson, "LESSON", request.title(), request.passingScore(), maxAttempts(request)));
        return ApiResponse.success("Lesson assessment created and completion gate enabled", Map.of("id", assessment.getId(), "title", assessment.getTitle(), "level", assessment.getAssessmentLevel()));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<Map<String,Object>>> courseAssessments(@PathVariable UUID courseId) {
        Course course = courses.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found"));
        authorization.assertCanManage(course);
        return ApiResponse.success("Course assessments loaded", assessments.findByCourseId(courseId).stream()
                .map(a -> Map.<String,Object>of("id", a.getId(), "title", a.getTitle(), "level", a.getAssessmentLevel(),
                        "passingScore", a.getPassingScore(), "maxAttempts", a.getMaxAttempts()))
                .toList());
    }

    @GetMapping("/question-bank/courses/{courseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<Map<String,Object>>> questionBank(@PathVariable UUID courseId,
                                                                          @RequestParam(required = false) String difficultyLevel,
                                                                          @RequestParam(required = false) String questionType) {
        Course course = courses.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found"));
        authorization.assertCanManage(course);
        String difficulty = difficultyLevel == null ? null : difficultyLevel.trim().toUpperCase();
        String type = questionType == null ? null : questionType.trim().toUpperCase();
        var result = questions.findByAssessmentCourseId(courseId).stream()
                .filter(q -> difficulty == null || difficulty.equals(q.getDifficultyLevel()))
                .filter(q -> type == null || type.equals(q.getQuestionType()))
                .map(q -> Map.<String,Object>of(
                        "id", q.getId(), "questionText", q.getQuestionText(), "questionType", q.getQuestionType(),
                        "points", q.getPoints(), "difficultyLevel", q.getDifficultyLevel(),
                        "sourceAssessmentId", q.getAssessment().getId(), "sourceAssessmentTitle", q.getAssessment().getTitle(),
                        "options", options.findByQuestionId(q.getId()).stream()
                                .map(o -> Map.<String,Object>of("id", o.getId(), "optionText", o.getOptionText(), "correct", o.isCorrect())).toList()))
                .toList();
        return ApiResponse.success("Question bank loaded", result);
    }

    @PostMapping("/{assessmentId}/questions/{questionId}/reuse")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional
    public ApiResponse<Map<String,Object>> reuseQuestion(@PathVariable UUID assessmentId, @PathVariable UUID questionId) {
        Assessment target = assessments.findById(assessmentId).orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        authorization.assertCanManage(target.getCourse());
        Question source = questions.findById(questionId).orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!source.getAssessment().getCourse().getId().equals(target.getCourse().getId())) {
            throw new IllegalArgumentException("Question and target assessment must belong to the same course");
        }
        Question copy = questions.saveAndFlush(new Question(target, source.getQuestionText(), source.getQuestionType(), source.getPoints(), source.getDifficultyLevel()));
        options.saveAll(options.findByQuestionId(source.getId()).stream()
                .map(o -> new QuestionOption(copy, o.getOptionText(), o.isCorrect())).toList());
        return ApiResponse.success("Question reused", Map.of("id", copy.getId(), "assessmentId", target.getId()));
    }

    private int maxAttempts(CreateAssessmentRequest request) { return request.maxAttempts() == null ? 3 : request.maxAttempts(); }

    @PostMapping("/{assessmentId}/questions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    @Transactional
    public ApiResponse<Map<String,Object>> question(@PathVariable UUID assessmentId,
                                                     @Valid @RequestBody CreateQuestionRequest request) {
        Assessment assessment = assessments.findById(assessmentId).orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        authorization.assertCanManage(assessment.getCourse());
        if (request.options().stream().noneMatch(CreateQuestionRequest.Option::correct)) {
            throw new IllegalArgumentException("At least one correct option is required");
        }
        Question question = questions.saveAndFlush(new Question(assessment, request.questionText(), request.questionType() == null ? "SINGLE_CHOICE" : request.questionType(), request.points(), request.difficultyLevel()));
        request.options().forEach(option -> options.save(new QuestionOption(question, option.text(), option.correct())));
        options.flush();
        return ApiResponse.success("Question created", Map.of("id", question.getId()));
    }
}
