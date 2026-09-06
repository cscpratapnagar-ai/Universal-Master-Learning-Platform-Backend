package com.masterlearning.platform.modules.course.dto.response;

import java.util.UUID;

public record AdaptiveNextActionResponse(
        String action,
        UUID lessonId,
        String lessonTitle,
        String reason,
        double priority,
        double mastery,
        boolean prerequisiteBlocked,
        UUID prerequisiteLessonId
) {}
