package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.LearningConceptMappingRepository;
import com.masterlearning.platform.modules.ai.repository.SemanticRagRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConceptAwareTutorRetriever {
    private static final double WEAK_CONCEPT_BOOST = 0.15;
    private static final double RELATED_CONCEPT_BOOST = 0.08;

    private final SemanticRagService semanticRag;
    private final LearningConceptMappingRepository mappings;

    public ConceptAwareTutorRetriever(SemanticRagService semanticRag, LearningConceptMappingRepository mappings) {
        this.semanticRag = semanticRag;
        this.mappings = mappings;
    }

    public List<SemanticRagRepository.SemanticChunk> retrieve(UUID courseId, String question, List<String> weakConcepts, int limit) {
        int safeLimit = Math.max(1, limit);
        List<SemanticRagRepository.SemanticChunk> base = semanticRag.search(courseId, question, Math.max(safeLimit, 5));
        if (base.isEmpty() || weakConcepts == null || weakConcepts.isEmpty()) return base.stream().limit(safeLimit).toList();
        Set<String> weak = weakConcepts.stream().filter(Objects::nonNull).map(this::normalize).filter(s -> !s.isBlank()).collect(java.util.stream.Collectors.toSet());
        if (weak.isEmpty()) return base.stream().limit(safeLimit).toList();
        List<UUID> lessonIds = base.stream().map(SemanticRagRepository.SemanticChunk::lessonId).distinct().toList();
        Map<UUID, List<String>> hierarchy = mappings.findConceptHierarchyNamesForLessons(lessonIds);
        return base.stream()
                .map(chunk -> new Boosted(chunk, Math.min(1.0, chunk.relevance() + conceptBoost(hierarchy.getOrDefault(chunk.lessonId(), List.of()), weak))))
                .sorted(Comparator.comparingDouble(Boosted::score).reversed())
                .limit(safeLimit)
                .map(b -> new SemanticRagRepository.SemanticChunk(b.chunk().lessonId(), b.chunk().lessonTitle(), b.chunk().content(), b.score()))
                .toList();
    }

    private double conceptBoost(List<String> mappedNames, Set<String> weak) {
        double best = 0.0;
        for (String mappedName : mappedNames) {
            String mapped = normalize(mappedName);
            if (mapped.isBlank()) continue;
            for (String target : weak) {
                if (mapped.equals(target)) best = Math.max(best, WEAK_CONCEPT_BOOST);
                Set<String> mappedTokens = tokens(mapped);
                Set<String> targetTokens = tokens(target);
                if (!mappedTokens.isEmpty() && !targetTokens.isEmpty()) {
                    long overlap = targetTokens.stream().filter(mappedTokens::contains).count();
                    double ratio = (double) overlap / targetTokens.size();
                    if (ratio >= 0.5) best = Math.max(best, WEAK_CONCEPT_BOOST * ratio);
                }
                if (mapped.contains(target) || target.contains(mapped)) best = Math.max(best, RELATED_CONCEPT_BOOST);
            }
        }
        return best;
    }

    private Set<String> tokens(String value) {
        Set<String> result = new HashSet<>();
        for (String token : value.split("[^a-z0-9]+")) if (!token.isBlank()) result.add(token);
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private record Boosted(SemanticRagRepository.SemanticChunk chunk, double score) {}
}
