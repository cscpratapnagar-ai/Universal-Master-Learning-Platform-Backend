package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.LearningConceptMappingRepository;
import com.masterlearning.platform.modules.ai.repository.SemanticRagRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConceptAwareTutorRetrieverTest {

    @Test
    void boostsLessonMappedToWeakConcept() {
        UUID courseId = UUID.randomUUID();
        UUID weakLessonId = UUID.randomUUID();
        UUID otherLessonId = UUID.randomUUID();

        SemanticRagService semanticRag = mock(SemanticRagService.class);
        LearningConceptMappingRepository mappings = mock(LearningConceptMappingRepository.class);
        ConceptAwareTutorRetriever retriever = new ConceptAwareTutorRetriever(semanticRag, mappings);

        SemanticRagRepository.SemanticChunk weakLesson =
                new SemanticRagRepository.SemanticChunk(weakLessonId, "Quadratics", "quadratic content", 0.70);
        SemanticRagRepository.SemanticChunk otherLesson =
                new SemanticRagRepository.SemanticChunk(otherLessonId, "Geometry", "geometry content", 0.80);

        when(semanticRag.search(courseId, "quadratic equations", 5)).thenReturn(List.of(otherLesson, weakLesson));
        when(mappings.findConceptHierarchyNamesForLessons(List.of(otherLessonId, weakLessonId)))
                .thenReturn(Map.of(weakLessonId, List.of("Quadratic Equations")));

        List<SemanticRagRepository.SemanticChunk> result =
                retriever.retrieve(courseId, "quadratic equations", List.of("quadratic equations"), 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).lessonId()).isEqualTo(weakLessonId);
        assertThat(result.get(0).relevance()).isEqualTo(0.85);
    }

    @Test
    void includesParentConceptFromHierarchy() {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        SemanticRagService semanticRag = mock(SemanticRagService.class);
        LearningConceptMappingRepository mappings = mock(LearningConceptMappingRepository.class);
        ConceptAwareTutorRetriever retriever = new ConceptAwareTutorRetriever(semanticRag, mappings);

        SemanticRagRepository.SemanticChunk chunk =
                new SemanticRagRepository.SemanticChunk(lessonId, "Factoring", "factoring content", 0.60);
        when(semanticRag.search(courseId, "quadratic equations", 5)).thenReturn(List.of(chunk));
        when(mappings.findConceptHierarchyNamesForLessons(List.of(lessonId)))
                .thenReturn(Map.of(lessonId, List.of("Algebra", "Quadratic Equations")));

        List<SemanticRagRepository.SemanticChunk> result =
                retriever.retrieve(courseId, "quadratic equations", List.of("quadratic equations"), 1);

        assertThat(result).singleElement().extracting(SemanticRagRepository.SemanticChunk::relevance)
                .isEqualTo(0.75);
        verify(mappings).findConceptHierarchyNamesForLessons(List.of(lessonId));
    }

    @Test
    void preservesSemanticOrderWhenNoWeakConceptsAreAvailable() {
        UUID courseId = UUID.randomUUID();
        SemanticRagService semanticRag = mock(SemanticRagService.class);
        LearningConceptMappingRepository mappings = mock(LearningConceptMappingRepository.class);
        ConceptAwareTutorRetriever retriever = new ConceptAwareTutorRetriever(semanticRag, mappings);

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        SemanticRagRepository.SemanticChunk first =
                new SemanticRagRepository.SemanticChunk(firstId, "First", "first", 0.90);
        SemanticRagRepository.SemanticChunk second =
                new SemanticRagRepository.SemanticChunk(secondId, "Second", "second", 0.70);
        when(semanticRag.search(courseId, "explain", 5)).thenReturn(List.of(first, second));

        List<SemanticRagRepository.SemanticChunk> result =
                retriever.retrieve(courseId, "explain", List.of(), 1);

        assertThat(result).singleElement().isEqualTo(first);
    }
}
