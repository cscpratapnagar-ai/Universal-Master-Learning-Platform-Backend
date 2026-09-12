package com.masterlearning.platform.modules.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record AdaptiveAssessmentSession(
        UUID sessionId,
        UUID assessmentId,
        String status,
        int questionsAnswered,
        int questionLimit,
        Question question
) {
    public record Question(UUID id, String questionText, String questionType, int points, String difficulty,
                           List<Option> options) {}
    public record Option(UUID id, String optionText) {}
}
