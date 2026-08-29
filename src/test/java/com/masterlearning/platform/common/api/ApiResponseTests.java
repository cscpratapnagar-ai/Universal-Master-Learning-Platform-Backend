package com.masterlearning.platform.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTests {

    @Test
    void successResponseShouldHaveExpectedShape() {
        ApiResponse<String> response = ApiResponse.success("OK", "data");

        assertTrue(response.success());
        assertEquals("OK", response.message());
        assertEquals("data", response.data());
        assertNotNull(response.timestamp());
    }
}