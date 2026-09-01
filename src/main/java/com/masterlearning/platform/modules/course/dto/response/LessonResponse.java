package com.masterlearning.platform.modules.course.dto.response;

import java.util.List;
import java.util.UUID;

public record LessonResponse(
        UUID id,
        String title,
        String contentType,
        String content,
        int sortOrder,
        boolean completed,
        boolean locked,
        List<UUID> unmetPrerequisiteLessonIds
) {}
