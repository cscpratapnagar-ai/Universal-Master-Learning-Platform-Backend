package com.masterlearning.platform.modules.ai.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TutorRetrievalRerankerTest {
    private final TutorRetrievalReranker reranker = new TutorRetrievalReranker();

    @Test
    void boostsExactTitleEvidence() {
        List<Candidate> result = reranker.rerank(
                "binary search",
                List.of(
                        new Candidate("1", "Arrays", "General data structures", 0.80),
                        new Candidate("2", "Binary Search", "Searching an ordered array", 0.70)),
                2);

        assertThat(result.get(0).lessonIdKey()).isEqualTo("2");
    }

    @Test
    void keepsLessonDiversityBeforeRepeatingSameLesson() {
        List<Candidate> result = reranker.rerank(
                "java classes",
                List.of(
                        new Candidate("1", "Classes", "Java classes and objects", 0.95),
                        new Candidate("1", "Classes", "Constructors in Java classes", 0.90),
                        new Candidate("2", "Inheritance", "Java inheritance", 0.80)),
                2);

        assertThat(result).extracting(Candidate::lessonIdKey)
                .containsExactly("1", "2");
    }

    @Test
    void clampsInvalidFirstStageRelevance() {
        List<Candidate> result = reranker.rerank(
                "algebra",
                List.of(new Candidate("1", "Algebra", "equations", 4.0)),
                1);

        assertThat(result).hasSize(1);
    }

    private record Candidate(String lessonIdKey, String lessonTitle, String content, double relevance)
            implements TutorRetrievalReranker.Candidate {
    }
}
