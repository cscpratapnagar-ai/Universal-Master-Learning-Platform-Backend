package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.LearningConceptRepository;
import com.masterlearning.platform.modules.ai.repository.SemanticRagRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConceptAwareTutorRetriever {
    private static final double WEAK_CONCEPT_BOOST = 0.15;
    private static final double PARENT_CONCEPT_BOOST = 0.08;

    private final SemanticRagService semanticRag;
    private final LearningConceptRepository concepts;
    private final TutorRetrievalReranker reranker;

    public ConceptAwareTutorRetriever(SemanticRagService semanticRag,
                                      LearningConceptRepository concepts,
                                      TutorRetrievalReranker reranker) {
        this.semanticRag = semanticRag;
        this.concepts = concepts;
        this.reranker = reranker;
    }

    public List<SemanticRagRepository.SemanticChunk> retrieve(
            UUID courseId, String question, List<String> weakConcepts, int limit) {
        List<SemanticRagRepository.SemanticChunk> base = semanticRag.search(courseId, question, Math.max(limit, 5));
        if (base.isEmpty() || weakConcepts == null || weakConcepts.isEmpty()) return base.stream().limit(limit).toList();

        Set<String> weak = weakConcepts.stream().filter(Objects::nonNull).map(this::normalize).filter(s -> !s.isBlank()).collect(java.util.stream.Collectors.toSet());
        List<Boosted> boosted = new ArrayList<>();
        for (var chunk : base) {
            List<String> mapped = concepts.findNamesForLesson(chunk.lessonId());
            double boost = mapped.stream().mapToDouble(name -> conceptBoost(name, weak)).max().orElse(0.0);
            boosted.add(new Boosted(chunk, Math.min(1.0, chunk.relevance() + boost)));
        }
        return boosted.stream()
                .sorted(Comparator.comparingDouble(Boosted::score).reversed())
                .limit(limit)
                .map(b -> new SemanticRagRepository.SemanticChunk(b.chunk().lessonId(), b.chunk().lessonTitle(), b.chunk().content(), b.score()))
                .toList();
    }

    private double conceptBoost(String mappedName, Set<String> weak) {
        String mapped = normalize(mappedName);
        if (mapped.isBlank()) return 0.0;
        for (String target : weak) {
            if (mapped.equals(target)) return WEAK_CONCEPT_BOOST;
            Set<String> mappedTokens = tokens(mapped);
            Set<String> targetTokens = tokens(target);
            if (!mappedTokens.isEmpty() && !targetTokens.isEmpty()) {
                long overlap = targetTokens.stream().filter(mappedTokens::contains).count();
                double ratio = (double) overlap / targetTokens.size();
                if (ratio >= 0.5) return WEAK_CONCEPT_BOOST * ratio;
            }
            if (mapped.contains(target) || target.contains(mapped)) return PARENT_CONCEPT_BOOST;
        }
        return 0.0;
    }

    private Set<String> tokens(String value) {
        return new HashSet<>(Arrays.asList(value.split("[^a-z0-9]+")));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private record Boosted(SemanticRagRepository.SemanticChunk chunk, double score) {}
}
