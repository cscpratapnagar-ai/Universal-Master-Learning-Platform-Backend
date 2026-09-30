package com.masterlearning.platform.modules.academy;

public record AcademyLearnerOverviewResponse(
        long activeLearners,
        long totalLearners,
        long activeStudents
) {}
