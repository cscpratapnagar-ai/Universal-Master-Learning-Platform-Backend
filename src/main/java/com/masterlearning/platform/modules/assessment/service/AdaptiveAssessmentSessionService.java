package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.request.AdaptiveAssessmentAnswerRequest;
import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentSession;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAnswer;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.entity.QuestionOption;
import com.masterlearning.platform.modules.assessment.repository.*;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AdaptiveAssessmentSessionService {
    private static final int DEFAULT_QUESTION_LIMIT = 10;

    private final JdbcTemplate jdbc;
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final AssessmentAttemptRepository attempts;
    private final AssessmentAnswerRepository answers;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;
    private final AdaptiveQuestionSelectionService selector;
    private final MasteryEngine masteryEngine;

    public AdaptiveAssessmentSessionService(JdbcTemplate jdbc, AssessmentRepository assessments,
                                            QuestionRepository questions, QuestionOptionRepository options,
                                            AssessmentAttemptRepository attempts, AssessmentAnswerRepository answers,
                                            UserRepository users, EnrollmentRepository enrollments,
                                            AdaptiveQuestionSelectionService selector, MasteryEngine masteryEngine) {
        this.jdbc = jdbc; this.assessments = assessments; this.questions = questions; this.options = options;
        this.attempts = attempts; this.answers = answers; this.users = users; this.enrollments = enrollments;
        this.selector = selector; this.masteryEngine = masteryEngine;
    }

    @Transactional
    public AdaptiveAssessmentSession start(UUID assessmentId, UUID userId) {
        Assessment assessment = getAssessment(assessmentId);
        requireEnrollment(assessment, userId);
        if (attempts.countByAssessmentIdAndUserId(assessmentId, userId) >= assessment.getMaxAttempts())
            throw new IllegalStateException("Maximum assessment attempts reached");

        Optional<SessionRow> existing = activeSession(assessmentId, userId);
        if (existing.isPresent()) return view(existing.get(), assessment);

        UUID sessionId = UUID.randomUUID();
        jdbc.update("INSERT INTO assessment_sessions(id, assessment_id, user_id, status, questions_answered) VALUES (?, ?, ?, 'ACTIVE', 0)",
                sessionId, assessmentId, userId);
        return nextInternal(sessionId, assessment, userId, Set.of(), 0);
    }

    @Transactional
    public AdaptiveAssessmentSession answer(UUID sessionId, AdaptiveAssessmentAnswerRequest request, UUID userId) {
        SessionRow session = getSession(sessionId, userId);
        if (!"ACTIVE".equals(session.status())) throw new IllegalStateException("Assessment session is not active");
        Assessment assessment = getAssessment(session.assessmentId());
        requireEnrollment(assessment, userId);
        if (!session.currentQuestionId().equals(request.questionId())) throw new IllegalArgumentException("Question is not the active session question");

        Question question = questions.findById(request.questionId()).orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!questionAssessmentId(question).equals(assessment.getId())) throw new IllegalArgumentException("Question does not belong to assessment");
        QuestionOption selected = request.selectedOptionId() == null ? null : options.findById(request.selectedOptionId())
                .orElseThrow(() -> new IllegalArgumentException("Selected option not found"));
        if (selected != null && !options.findByQuestionId(question.getId()).stream().anyMatch(o -> o.getId().equals(selected.getId())))
            throw new IllegalArgumentException("Selected option does not belong to the question");
        boolean correct = selected != null && selected.isCorrect();

        jdbc.update("INSERT INTO assessment_session_answers(id, session_id, question_id, selected_option_id, correct) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(), sessionId, question.getId(), selected == null ? null : selected.getId(), correct);
        int answered = session.questionsAnswered() + 1;
        Set<UUID> excluded = answeredQuestionIds(sessionId);
        List<Question> pool = questions.findByAssessmentId(assessment.getId());
        boolean complete = answered >= Math.min(DEFAULT_QUESTION_LIMIT, pool.size()) || answered >= pool.size();
        if (complete) return completeSession(session, assessment, userId, answered);
        return nextInternal(sessionId, assessment, userId, excluded, answered);
    }

    private AdaptiveAssessmentSession nextInternal(UUID sessionId, Assessment assessment, UUID userId, Set<UUID> excluded, int answered) {
        var decision = selector.select(assessment, userId, excluded);
        jdbc.update("UPDATE assessment_sessions SET current_question_id = ?, questions_answered = ? WHERE id = ?",
                decision.selectedQuestionId(), answered, sessionId);
        SessionRow updated = getSession(sessionId, userId);
        return view(updated, assessment);
    }

    private AdaptiveAssessmentSession completeSession(SessionRow session, Assessment assessment, UUID userId, int answered) {
        List<AnswerRow> rows = jdbc.query("SELECT question_id, selected_option_id, correct FROM assessment_session_answers WHERE session_id = ? ORDER BY answered_at, id",
                (rs, n) -> new AnswerRow(rs.getObject("question_id", UUID.class), rs.getObject("selected_option_id", UUID.class), rs.getBoolean("correct")), session.id());
        List<Question> assessmentQuestions = questions.findByAssessmentId(assessment.getId());
        int totalPoints = assessmentQuestions.stream().mapToInt(Question::getPoints).sum();
        int earned = rows.stream().filter(AnswerRow::correct).mapToInt(r -> questions.findById(r.questionId()).map(Question::getPoints).orElse(0)).sum();
        int score = totalPoints == 0 ? 0 : (int) Math.round(earned * 100.0 / totalPoints);
        boolean passed = score >= assessment.getPassingScore();
        var user = users.findById(userId).orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        int attemptNumber = (int) attempts.countByAssessmentIdAndUserId(assessment.getId(), userId) + 1;
        AssessmentAttempt attempt = attempts.saveAndFlush(new AssessmentAttempt(assessment, user, attemptNumber, score, passed, masteryEngine.assessmentMasteryLevel(score)));
        for (AnswerRow row : rows) {
            Question q = questions.findById(row.questionId()).orElseThrow();
            QuestionOption option = row.selectedOptionId() == null ? null : options.findById(row.selectedOptionId()).orElse(null);
            answers.save(new AssessmentAnswer(attempt, q, option, row.correct(), row.correct() ? q.getPoints() : 0));
        }
        jdbc.update("UPDATE assessment_sessions SET status='COMPLETED', questions_answered=?, current_question_id=NULL, completed_at=? WHERE id=?",
                answered, Instant.now(), session.id());
        return new AdaptiveAssessmentSession(session.id(), assessment.getId(), "COMPLETED", answered,
                Math.min(DEFAULT_QUESTION_LIMIT, assessmentQuestions.size()), null);
    }

    private AdaptiveAssessmentSession view(SessionRow session, Assessment assessment) {
        Question question = session.currentQuestionId() == null ? null : questions.findById(session.currentQuestionId()).orElse(null);
        return new AdaptiveAssessmentSession(session.id(), assessment.getId(), session.status(), session.questionsAnswered(),
                Math.min(DEFAULT_QUESTION_LIMIT, questions.findByAssessmentId(assessment.getId()).size()), question == null ? null : questionView(question));
    }

    private AdaptiveAssessmentSession.Question questionView(Question q) {
        List<AdaptiveAssessmentSession.Option> optionViews = options.findByQuestionId(q.getId()).stream()
                .map(o -> new AdaptiveAssessmentSession.Option(o.getId(), o.getOptionText())).toList();
        return new AdaptiveAssessmentSession.Question(q.getId(), q.getQuestionText(), q.getQuestionType(), q.getPoints(), "ADAPTIVE", optionViews);
    }

    private Set<UUID> answeredQuestionIds(UUID sessionId) {
        return new HashSet<>(jdbc.query("SELECT question_id FROM assessment_session_answers WHERE session_id=?",
                (rs, n) -> rs.getObject("question_id", UUID.class), sessionId));
    }

    private Optional<SessionRow> activeSession(UUID assessmentId, UUID userId) {
        return jdbc.query("SELECT id, assessment_id, user_id, status, current_question_id, questions_answered FROM assessment_sessions WHERE assessment_id=? AND user_id=? AND status='ACTIVE'",
                (rs, n) -> new SessionRow(rs.getObject("id", UUID.class), rs.getObject("assessment_id", UUID.class),
                        rs.getObject("user_id", UUID.class), rs.getString("status"), rs.getObject("current_question_id", UUID.class), rs.getInt("questions_answered")),
                assessmentId, userId).stream().findFirst();
    }

    private SessionRow getSession(UUID id, UUID userId) {
        return jdbc.query("SELECT id, assessment_id, user_id, status, current_question_id, questions_answered FROM assessment_sessions WHERE id=? AND user_id=?",
                (rs, n) -> new SessionRow(rs.getObject("id", UUID.class), rs.getObject("assessment_id", UUID.class),
                        rs.getObject("user_id", UUID.class), rs.getString("status"), rs.getObject("current_question_id", UUID.class), rs.getInt("questions_answered")), id, userId)
                .stream().findFirst().orElseThrow(() -> new EntityNotFoundException("Assessment session not found"));
    }

    private Assessment getAssessment(UUID id) { return assessments.findById(id).orElseThrow(() -> new EntityNotFoundException("Assessment not found")); }

    private UUID questionAssessmentId(Question question) {
        return question.getAssessment().getId();
    }

    private void requireEnrollment(Assessment assessment, UUID userId) {
        if (!enrollments.existsByCourseIdAndUserId(assessment.getCourse().getId(), userId))
            throw new org.springframework.security.access.AccessDeniedException("You are not enrolled in this course");
    }

    private record SessionRow(UUID id, UUID assessmentId, UUID userId, String status, UUID currentQuestionId, int questionsAnswered) {}
    private record AnswerRow(UUID questionId, UUID selectedOptionId, boolean correct) {}
}
