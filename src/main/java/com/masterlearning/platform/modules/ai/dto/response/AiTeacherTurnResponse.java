package com.masterlearning.platform.modules.ai.dto.response;

public record AiTeacherTurnResponse(
        String phase,
        String teacherText,
        String teachingMode,
        String visualMode,
        String nextPhase,
        boolean askStudent,
        String studentPrompt,
        boolean lectureComplete
) {}
