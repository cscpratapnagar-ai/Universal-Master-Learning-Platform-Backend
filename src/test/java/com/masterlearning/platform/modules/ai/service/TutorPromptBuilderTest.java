package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TutorPromptBuilderTest {
    @Test
    void includesWeakConceptsInLearnerContext() {
        LearnerTutorContext learner = new LearnerTutorContext(
                UUID.randomUUID(), 55.0, "DEVELOPING", "MEDIUM", "BUILDING",
                List.of("Algebra Assessment"), List.of("Quadratic Equations", "Factoring"),
                "CONTINUE_LEARNING", "BALANCED_EXPLANATION");

        String prompt = new TutorPromptBuilder().build(
                "How do I solve this?", "[Lesson: Quadratics]\nCourse material", learner, List.of());

        assertThat(prompt).contains("Weak concepts/skills: Quadratic Equations, Factoring");
        assertThat(prompt).contains("When a weak concept is relevant");
    }

    @Test
    void handlesMissingWeakConceptsSafely() {
        LearnerTutorContext learner = new LearnerTutorContext(
                UUID.randomUUID(), 80.0, "PROFICIENT", "LOW", "ON_TRACK",
                List.of("No specific weak area identified"), null,
                "CONTINUE_LEARNING", "BALANCED_EXPLANATION");

        String prompt = new TutorPromptBuilder().build("Explain this", "Course material", learner, null);

        assertThat(prompt).contains("Weak concepts/skills: No specific weak concept identified");
    }
}
