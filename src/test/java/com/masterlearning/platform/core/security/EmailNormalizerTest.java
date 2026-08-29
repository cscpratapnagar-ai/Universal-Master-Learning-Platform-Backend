package com.masterlearning.platform.core.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailNormalizerTest {
    @Test
    void normalizesCaseAndWhitespace() {
        assertEquals("test@example.com", EmailNormalizer.normalize(" Test@Example.COM "));
    }

    @Test
    void rejectsBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> EmailNormalizer.normalize(" "));
    }
}