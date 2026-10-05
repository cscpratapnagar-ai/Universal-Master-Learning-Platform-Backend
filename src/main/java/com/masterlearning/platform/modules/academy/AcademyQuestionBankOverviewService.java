package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyQuestionBankOverviewService {

    private final QuestionRepository questions;
    private final AcademyScopeService scope;

    public AcademyQuestionBankOverviewService(QuestionRepository questions, AcademyScopeService scope) {
        this.questions = questions;
        this.scope = scope;
    }

    @Transactional(readOnly = true)
    public AcademyQuestionBankOverviewResponse getOverview() {
        long single = 0;
        long multiple = 0;
        long trueFalse = 0;
        var list = scope.isGlobal() ? questions.findAll() : scope.accessibleOrganizationIds().stream().flatMap(id -> questions.findByOrganizationId(id).stream()).toList();
        for (Question question : list) {
            String type = question.getQuestionType();
            if ("MULTIPLE_CHOICE".equalsIgnoreCase(type)) multiple++;
            else if ("TRUE_FALSE".equalsIgnoreCase(type)) trueFalse++;
            else single++;
        }
        return new AcademyQuestionBankOverviewResponse(list.size(), single, multiple, trueFalse);
    }
}
