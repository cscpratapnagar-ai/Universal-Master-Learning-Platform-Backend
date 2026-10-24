package com.masterlearning.platform.common.exception;

import com.masterlearning.platform.common.api.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler(new MockEnvironment());

    @Test
    void mapsConflictExceptionToHttp409WithConflictCode() {
        var response = handler.handleBusinessException(
                new ConflictException("Session time is no longer available"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertTrue(body != null);
        assertFalse(body.success());
        assertEquals(ErrorCode.CONFLICT.name(), body.code());
        assertEquals("Session time is no longer available", body.message());
    }

    @Test
    void mapsEntityNotFoundToHttp404() {
        var response = handler.handleEntityNotFound(
                new jakarta.persistence.EntityNotFoundException("Certificate not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertTrue(body != null);
        assertFalse(body.success());
        assertEquals("Certificate not found", body.message());
    }

    @Test
    void mapsIllegalArgumentExceptionToHttp400() {
        var response = handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid availability timezone"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertTrue(body != null);
        assertFalse(body.success());
        assertEquals(ErrorCode.BAD_REQUEST.name(), body.code());
        assertEquals("Invalid availability timezone", body.message());
    }

    @Test
    void hidesUnexpectedExceptionDetailsOutsideLocalProfile() {
        var response = handler.handleUnexpectedException(
                new IllegalStateException("sensitive database details"),
                new MockHttpServletRequest("POST", "/api/v1/private-teacher/sessions/request"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertTrue(body != null);
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.name(), body.code());
        assertEquals("An unexpected error occurred", body.message());
    }
}
