package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.request.AdaptiveAssessmentAnswerRequest;
import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentSession;
import com.masterlearning.platform.modules.assessment.service.AdaptiveAssessmentSessionService;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/assessments")
public class AdaptiveAssessmentSessionController {
    private final AdaptiveAssessmentSessionService sessions;

    public AdaptiveAssessmentSessionController(AdaptiveAssessmentSessionService sessions) {
        this.sessions = sessions;
    }

    @PostMapping("/{assessmentId}/adaptive-session/start")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AdaptiveAssessmentSession> start(@PathVariable UUID assessmentId) {
        return ApiResponse.success("Adaptive assessment session started",
                sessions.start(assessmentId, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/adaptive-session/{sessionId}/answer")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AdaptiveAssessmentSession> answer(@PathVariable UUID sessionId,
                                                         @Valid @RequestBody AdaptiveAssessmentAnswerRequest request) {
        return ApiResponse.success("Adaptive answer evaluated",
                sessions.answer(sessionId, request, SecurityUtils.getCurrentUserId()));
    }
}
