package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentEvaluatorAssignmentRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/examiner/assessments")
public class ExaminerAssessmentOverviewController {
    private final AssessmentRepository assessments;
    private final AssessmentAttemptRepository attempts;
    private final AssessmentEvaluatorAssignmentRepository assignments;
    private final UserRepository users;

    public ExaminerAssessmentOverviewController(AssessmentRepository assessments,
                                                AssessmentAttemptRepository attempts,
                                                AssessmentEvaluatorAssignmentRepository assignments,
                                                UserRepository users) {
        this.assessments = assessments;
        this.attempts = attempts;
        this.assignments = assignments;
        this.users = users;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EXAMINER') or hasAuthority('EXAM:OVERSIGHT')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> overview() {
        List<Map<String, Object>> result = assessments.findAll().stream().map(assessment -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("assessmentId", assessment.getId());
            item.put("assessmentTitle", assessment.getTitle());
            item.put("assessmentLevel", assessment.getAssessmentLevel());
            item.put("courseId", assessment.getCourse().getId());
            item.put("courseTitle", assessment.getCourse().getTitle());
            item.put("passingScore", assessment.getPassingScore());
            item.put("maxAttempts", assessment.getMaxAttempts());
            item.put("submittedAttemptCount", attempts.findByAssessmentIdOrderBySubmittedAtDesc(assessment.getId()).size());
            item.put("evaluatorAssignmentCount", assignments.findByAssessmentId(assessment.getId()).size());
            return item;
        }).toList();
        return ApiResponse.success("Examination assessment overview retrieved", result);
    }


    @GetMapping("/eligible-evaluators")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EXAMINER') or hasAuthority('EXAM:OVERSIGHT')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> eligibleEvaluators() {
        List<Map<String, Object>> result = users.findEnabledUsersByRoleCode("EVALUATOR").stream().map(user -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("evaluatorId", user.getId());
            item.put("email", user.getEmail());
            item.put("displayName", (user.getFirstName() + " " + (user.getLastName() == null ? "" : user.getLastName())).trim());
            return item;
        }).toList();
        return ApiResponse.success("Eligible evaluators retrieved", result);
    }

    @GetMapping("/{assessmentId}/evaluators")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EXAMINER') or hasAuthority('EXAM:OVERSIGHT')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> evaluators(@PathVariable UUID assessmentId) {
        var assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        List<Map<String, Object>> result = assignments.findByAssessmentId(assessmentId).stream().map(assignment -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("assignmentId", assignment.getId());
            item.put("evaluatorId", assignment.getEvaluator().getId());
            item.put("evaluatorEmail", assignment.getEvaluator().getEmail());
            item.put("assignedAt", assignment.getCreatedAt());
            item.put("assessmentId", assessment.getId());
            return item;
        }).toList();
        return ApiResponse.success("Assigned evaluators retrieved", result);
    }
}