package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.repository.LearningConceptMappingRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrerequisiteAwareTutorServiceTest {
    private final LearningConceptMappingRepository mappings = mock(LearningConceptMappingRepository.class);
    private final ConceptMasteryService mastery = mock(ConceptMasteryService.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final PrerequisiteAwareTutorService service = new PrerequisiteAwareTutorService(mappings, mastery, enrollments);

    @Test
    void detectsMissingDirectPrerequisite() {
        UUID target = UUID.randomUUID();
        UUID prerequisite = UUID.randomUUID();
        UUID course = UUID.randomUUID();
        UUID user = UUID.randomUUID();

        when(mastery.forLearner(course, user)).thenReturn(List.of(
                new ConceptMasteryService.ConceptMastery(prerequisite, "Algebra Basics", "CONCEPT", 40, 2)));
        when(mappings.findDirectPrerequisiteIds(List.of(target))).thenReturn(Map.of(target, List.of(prerequisite)));
        when(mappings.findDirectPrerequisiteIds(List.of(prerequisite))).thenReturn(Map.of());
        when(mappings.findActiveConceptNames(List.of(prerequisite))).thenReturn(Map.of(prerequisite, "Algebra Basics"));

        PrerequisiteAwareTutorService.Result result = service.analyze(course, user, List.of(target));

        assertTrue(result.prerequisiteBlocker());
        assertEquals(1, result.missingPrerequisites().size());
        assertEquals("Algebra Basics", result.missingPrerequisites().get(0).name());
        assertEquals(40, result.missingPrerequisites().get(0).mastery());
    }

    @Test
    void doesNotBlockWhenPrerequisiteMasteryIsSufficient() {
        UUID target = UUID.randomUUID();
        UUID prerequisite = UUID.randomUUID();
        UUID course = UUID.randomUUID();
        UUID user = UUID.randomUUID();

        when(mastery.forLearner(course, user)).thenReturn(List.of(
                new ConceptMasteryService.ConceptMastery(prerequisite, "Algebra Basics", "CONCEPT", 80, 5)));
        when(mappings.findDirectPrerequisiteIds(List.of(target))).thenReturn(Map.of(target, List.of(prerequisite)));
        when(mappings.findDirectPrerequisiteIds(List.of(prerequisite))).thenReturn(Map.of());

        PrerequisiteAwareTutorService.Result result = service.analyze(course, user, List.of(target));

        assertFalse(result.prerequisiteBlocker());
        assertTrue(result.missingPrerequisites().isEmpty());
        verify(mappings, never()).findActiveConceptNames(anyList());
    }

    @Test
    void followsTransitivePrerequisitesAndStopsAtMasteredNodes() {
        UUID target = UUID.randomUUID();
        UUID middle = UUID.randomUUID();
        UUID root = UUID.randomUUID();
        UUID course = UUID.randomUUID();
        UUID user = UUID.randomUUID();

        when(mastery.forLearner(course, user)).thenReturn(List.of(
                new ConceptMasteryService.ConceptMastery(middle, "Middle Concept", "CONCEPT", 75, 4),
                new ConceptMasteryService.ConceptMastery(root, "Foundation", "SKILL", 20, 1)));
        when(mappings.findDirectPrerequisiteIds(List.of(target))).thenReturn(Map.of(target, List.of(middle)));
        when(mappings.findDirectPrerequisiteIds(List.of(middle))).thenReturn(Map.of(middle, List.of(root)));
        when(mappings.findDirectPrerequisiteIds(List.of(root))).thenReturn(Map.of());
        when(mappings.findActiveConceptNames(List.of(root))).thenReturn(Map.of(root, "Foundation"));

        PrerequisiteAwareTutorService.Result result = service.analyze(course, user, List.of(target));

        assertTrue(result.prerequisiteBlocker());
        assertEquals(List.of("Foundation"), result.missingPrerequisites().stream().map(PrerequisiteAwareTutorService.MissingPrerequisite::name).toList());
    }
}
