package com.masterlearning.platform.modules.academy;

public record AcademyCourseOverviewResponse(
        long totalCourses,
        long publishedCourses,
        long draftCourses,
        long archivedCourses
) {}
