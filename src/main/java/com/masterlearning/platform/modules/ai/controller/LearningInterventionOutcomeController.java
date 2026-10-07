package com.masterlearning.platform.modules.ai.controller;
import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.request.LearningInterventionOutcomeRequest;
import com.masterlearning.platform.modules.ai.entity.LearningInterventionOutcome;
import com.masterlearning.platform.modules.ai.dto.response.LearningInterventionOutcomeSummary;
import com.masterlearning.platform.modules.ai.service.LearningInterventionOutcomeService;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai/interventions")
public class LearningInterventionOutcomeController {
    private final LearningInterventionOutcomeService service;
    public LearningInterventionOutcomeController(LearningInterventionOutcomeService service){this.service=service;}
    @PostMapping("/outcome") @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningInterventionOutcome> record(@PathVariable UUID enrollmentId,@Valid @RequestBody LearningInterventionOutcomeRequest request){
        return ApiResponse.success("Learning intervention outcome recorded",service.record(enrollmentId,SecurityUtils.getCurrentUserId(),request));
    }
    @GetMapping("/summary") @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningInterventionOutcomeSummary> summary(@PathVariable UUID enrollmentId){
        return ApiResponse.success("Learning intervention outcome summary loaded",service.summary(enrollmentId,SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/history") @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<LearningInterventionOutcome>> history(@PathVariable UUID enrollmentId){
        return ApiResponse.success("Learning intervention history loaded",service.recent(enrollmentId,SecurityUtils.getCurrentUserId()));
    }
}