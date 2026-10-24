package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentEvaluatorAssignment;
import com.masterlearning.platform.modules.assessment.repository.AssessmentEvaluatorAssignmentRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evaluator/assignments")
public class EvaluatorAssignmentController {
    private final AssessmentEvaluatorAssignmentRepository assignments;
    private final AssessmentRepository assessments;
    private final UserRepository users;

    public EvaluatorAssignmentController(AssessmentEvaluatorAssignmentRepository assignments,
                                         AssessmentRepository assessments, UserRepository users) {
        this.assignments = assignments;
        this.assessments = assessments;
        this.users = users;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','EXAMINER') or hasAuthority('EXAM:OVERSIGHT')")
    @Transactional
    public ApiResponse<Map<String, Object>> assign(@Valid @RequestBody CreateAssignmentRequest request) {
        var assessment = assessments.findById(request.assessmentId())
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        User evaluator = users.findById(request.evaluatorId())
                .orElseThrow(() -> new EntityNotFoundException("Evaluator user not found"));
        boolean evaluatorRole = evaluator.getRoles().stream()
                .anyMatch(role -> "EVALUATOR".equalsIgnoreCase(role.getCode()));
        if (!evaluatorRole) throw new IllegalArgumentException("Selected user does not have the EVALUATOR role");
        if (assignments.existsByAssessmentIdAndEvaluatorId(request.assessmentId(), request.evaluatorId())) {
            throw new IllegalArgumentException("Evaluator is already assigned to this assessment");
        }
        User assignedBy = users.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        try {
            AssessmentEvaluatorAssignment saved = assignments.saveAndFlush(
                    new AssessmentEvaluatorAssignment(assessment, evaluator, assignedBy));
            return ApiResponse.success("Evaluator assigned to assessment", Map.of(
                    "assignmentId", saved.getId(),
                    "assessmentId", assessment.getId(),
                    "assessmentTitle", assessment.getTitle(),
                    "evaluatorId", evaluator.getId()));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Evaluator is already assigned to this assessment");
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('EVALUATOR') or hasAuthority('ASSESSMENT:REVIEW')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> myAssignments() {
        UUID evaluatorId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> result = assignments.findByEvaluatorIdOrderByCreatedAtDesc(evaluatorId)
                .stream().map(assignment -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("assignmentId", assignment.getId());
                    item.put("assessmentId", assignment.getAssessment().getId());
                    item.put("assessmentTitle", assignment.getAssessment().getTitle());
                    item.put("courseId", assignment.getAssessment().getCourse().getId());
                    item.put("courseTitle", assignment.getAssessment().getCourse().getTitle());
                    item.put("assignedAt", assignment.getCreatedAt());
                    return item;
                }).toList();
        return ApiResponse.success("Evaluator assignments retrieved", result);
    }

    public record CreateAssignmentRequest(@NotNull UUID assessmentId, @NotNull UUID evaluatorId) {}
}
