package com.masterlearning.platform.modules.academy;

public record AcademyOverviewResponse(
        long activeUsers,
        long publishedCourses,
        long activePrograms,
        long publishedPrograms,
        long pausedPrograms,
        long completedPrograms
) {}
