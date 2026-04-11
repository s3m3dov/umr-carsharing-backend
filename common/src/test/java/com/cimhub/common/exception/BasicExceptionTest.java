package com.cimhub.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class BasicExceptionTest {

    @Test
    void forbiddenException_shouldExposeCodeAndMessage() {
        ForbiddenException ex = new ForbiddenException("Admin access required");

        assertEquals("Admin access required", ex.getMessage());
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        assertNotNull(ex.getBody());
    }

    @Test
    void resourceNotFoundException_shouldExposeCodeAndMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Trip not found");

        assertEquals("Trip not found", ex.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void unauthorizedException_shouldExposeCodeAndMessage() {
        UnauthorizedException ex = new UnauthorizedException("Token missing");

        assertEquals("Token missing", ex.getMessage());
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void userAlreadyExistsException_shouldExposeCodeAndMessage() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User exists");

        assertEquals("User exists", ex.getMessage());
        assertEquals(ErrorCode.USER_ALREADY_EXISTS, ex.getErrorCode());
    }
}
