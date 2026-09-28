package com.masterlearning.platform.modules.ai.dto.request;

public record AiTeacherTurnRequest(
        String topic,
        String language,
        String phase,
        String studentMessage,
        Integer lectureMinute
) {}
