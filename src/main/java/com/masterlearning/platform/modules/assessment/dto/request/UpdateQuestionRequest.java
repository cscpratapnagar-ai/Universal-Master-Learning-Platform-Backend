package com.masterlearning.platform.modules.assessment.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UpdateQuestionRequest(
        @NotBlank String questionText,
        String questionType,
        @Min(1) int points,
        String difficultyLevel,
        @NotEmpty List<Option> options
) {
    public record Option(@NotBlank String text, boolean correct) {}
}
