package com.masterlearning.platform.modules.academy;

public record AcademyQuestionBankOverviewResponse(
        long totalQuestions,
        long singleChoiceQuestions,
        long multipleChoiceQuestions,
        long trueFalseQuestions
) {}
