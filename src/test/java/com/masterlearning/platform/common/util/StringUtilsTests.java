package com.masterlearning.platform.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringUtilsTests {

    @Test
    void trimToNullShouldTrimText() {
        assertEquals("hello", StringUtils.trimToNull("  hello  "));
    }

    @Test
    void trimToNullShouldReturnNullForBlankText() {
        assertNull(StringUtils.trimToNull("   "));
    }
}