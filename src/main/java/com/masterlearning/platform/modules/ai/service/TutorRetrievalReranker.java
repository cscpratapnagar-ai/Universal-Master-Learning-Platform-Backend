package com.masterlearning.platform.modules.ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic second-stage reranker for tutor retrieval candidates.
 * It combines the first-stage relevance with exact phrase/title evidence and
 * applies light source diversity so one lesson cannot dominate the context.
 */
@Service
public class TutorRetrievalReranker {
    private static final double FIRST_STAGE_WEIGHT = 0.75;
    private static final double TITLE_WEIGHT = 0.15;
    private static final double PHRASE_WEIGHT = 0.10;

    public <T extends Candidate> List<T> rerank(String query, List<T> candidates, int limit) {
        if (candidates == null || candidates.isEmpty()) return List.of();
        int safeLimit = Math.max(1, limit);
        String normalizedQuery = normalize(query);
        Set<String> terms = tokenize(normalizedQuery);

        List<Scored<T>> scored = new ArrayList<>();
        for (T candidate : candidates) {
            String title = normalize(candidate.lessonTitle());
            String content = normalize(candidate.content());
            double titleScore = overlap(terms, title);
            double phraseScore = normalizedQuery.isBlank() || content.isBlank()
                    ? 0.0
                    : content.contains(normalizedQuery) ? 1.0 : 0.0;
            double score = FIRST_STAGE_WEIGHT * clamp(candidate.relevance())
                    + TITLE_WEIGHT * titleScore
                    + PHRASE_WEIGHT * phraseScore;
            scored.add(new Scored<>(candidate, score));
        }

        scored.sort(Comparator.comparingDouble(Scored<T>::score).reversed());
        List<T> result = new ArrayList<>();
        Set<String> usedLessons = new LinkedHashSet<>();

        // Prefer diversity for the first pass, then fill remaining slots by score.
        for (Scored<T> item : scored) {
            if (result.size() >= safeLimit) break;
            if (usedLessons.add(item.value().lessonIdKey())) result.add(item.value());
        }
        for (Scored<T> item : scored) {
            if (result.size() >= safeLimit) break;
            if (!result.contains(item.value())) result.add(item.value());
        }
        return result;
    }

    private double overlap(Set<String> terms, String value) {
        if (terms.isEmpty() || value.isBlank()) return 0.0;
        long matches = terms.stream().filter(value::contains).count();
        return (double) matches / terms.size();
    }

    private Set<String> tokenize(String value) {
        if (value.isBlank()) return Set.of();
        Set<String> stopwords = Set.of("the", "and", "for", "with", "what", "why", "how", "can", "this", "that", "from");
        Set<String> terms = new LinkedHashSet<>();
        for (String token : value.split("[^\\p{L}\\p{N}]+")) {
            if (token.length() >= 3 && !stopwords.contains(token)) terms.add(token);
        }
        return terms;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private record Scored<T>(T value, double score) {}

    public interface Candidate {
        String lessonTitle();
        String content();
        String lessonIdKey();
        double relevance();
    }
}
