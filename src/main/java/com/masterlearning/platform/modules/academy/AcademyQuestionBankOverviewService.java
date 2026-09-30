package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyQuestionBankOverviewService {

    private final QuestionRepository questions;

    public AcademyQuestionBankOverviewService(QuestionRepository questions) {
        this.questions = questions;
    }

    @Transactional(readOnly = true)
    public AcademyQuestionBankOverviewResponse getOverview() {
        long single = 0;
        long multiple = 0;
        long trueFalse = 0;
        for (Question question : questions.findAll()) {
            String type = question.getQuestionType();
            if ("MULTIPLE_CHOICE".equalsIgnoreCase(type)) multiple++;
            else if ("TRUE_FALSE".equalsIgnoreCase(type)) trueFalse++;
            else single++;
        }
        return new AcademyQuestionBankOverviewResponse(questions.count(), single, multiple, trueFalse);
    }
}
