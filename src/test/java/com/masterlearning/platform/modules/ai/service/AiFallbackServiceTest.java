package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.GroundedTutorResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AiFallbackServiceTest {
    private final AiFallbackService service = new AiFallbackService();

    @Test
    void unavailableFallbackNeverClaimsLlmWasUsed() {
        UUID enrollmentId = UUID.randomUUID();
        var source = new GroundedTutorResponse.Source(UUID.randomUUID(), "Algebra", 0.9);
        GroundedTutorResponse response = service.unavailable(enrollmentId, List.of(source));
        assertEquals(enrollmentId, response.enrollmentId());
        assertTrue(response.grounded());
        assertFalse(response.llmUsed());
        assertEquals(1, response.sources().size());
    }

    @Test
    void unconfiguredFallbackIsSafeWithoutSources() {
        GroundedTutorResponse response = service.unconfigured(UUID.randomUUID(), List.of());
        assertFalse(response.grounded());
        assertFalse(response.llmUsed());
        assertTrue(response.sources().isEmpty());
        assertFalse(response.response().isBlank());
    }
}
