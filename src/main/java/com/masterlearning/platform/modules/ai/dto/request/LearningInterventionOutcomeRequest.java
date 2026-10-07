package com.masterlearning.platform.modules.ai.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record LearningInterventionOutcomeRequest(
    @NotBlank @Size(max=60) String interventionType, UUID targetLessonId,
    @NotBlank @Size(max=40) String outcome, Double masteryDelta, @Size(max=2000) String notes) {}