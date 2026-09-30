package com.masterlearning.platform.modules.academy;

public record AcademyTeacherOverviewResponse(
        long activeTeachers,
        long totalTeachers,
        long activeInstructors
) {}
