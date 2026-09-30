package com.masterlearning.platform.modules.academy;

public record AcademyProgramOverviewResponse(
        long totalPrograms,
        long draftPrograms,
        long publishedPrograms,
        long activePrograms,
        long pausedPrograms,
        long completedPrograms,
        long archivedPrograms
) {}
