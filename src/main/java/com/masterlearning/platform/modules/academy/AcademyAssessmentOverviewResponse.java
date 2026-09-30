package com.masterlearning.platform.modules.academy;

public record AcademyAssessmentOverviewResponse(
        long totalAssessments,
        long assessmentsWithCourse,
        long averageAttempts
) {}
