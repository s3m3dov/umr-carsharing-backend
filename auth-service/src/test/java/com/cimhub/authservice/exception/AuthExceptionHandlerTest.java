package com.cimhub.authservice.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ErrorCode;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.filter.RequestIdFilter;
import com.cimhub.common.testsupport.WebRequests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;

class AuthExceptionHandlerTest {

    private AuthExceptionHandler authExceptionHandler;

    @BeforeEach
    void setUp() {
        authExceptionHandler = new AuthExceptionHandler();
    }

    @AfterEach
    void tearDown() {
        MDC.remove(RequestIdFilter.MDC_KEY);
    }

    @Test
    void handleBadCredentials_shouldReturnUnauthorizedErrorPayload() {
        ServletWebRequest request = WebRequests.mockServletRequest("/api/auth/login", "header-req");

        ResponseEntity<ApiError> response =
                authExceptionHandler.handleBadCredentials(new BadCredentialsException("bad credentials"), request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED.code(), response.getBody().code());
        assertEquals("Invalid credentials", response.getBody().message());
        assertEquals("/api/auth/login", response.getBody().error().path());
        assertEquals("header-req", response.getBody().requestId());
    }

    @Test
    void handleUnauthorized_shouldReturnUnauthorizedErrorPayload() {
        ServletWebRequest request = WebRequests.mockServletRequest("/api/auth/validate", "header-req-2");

        ResponseEntity<ApiError> response =
                authExceptionHandler.handleUnauthorized(new UnauthorizedException("Invalid or expired token"), request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED.code(), response.getBody().code());
        assertEquals("Invalid or expired token", response.getBody().message());
        assertEquals("/api/auth/validate", response.getBody().error().path());
        assertEquals("header-req-2", response.getBody().requestId());
    }

    @Test
    void handleUserAlreadyExists_shouldReturnConflictErrorPayload() {
        ServletWebRequest request = WebRequests.mockServletRequest("/api/auth/signup", "header-req-3");

        ResponseEntity<ApiError> response = authExceptionHandler.handleUserAlreadyExists(
                new UserAlreadyExistsException("User already exists"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.USER_ALREADY_EXISTS.code(), response.getBody().code());
        assertEquals("User already exists", response.getBody().message());
        assertEquals("/api/auth/signup", response.getBody().error().path());
        assertEquals("header-req-3", response.getBody().requestId());
    }

    @Test
    void handleBadRequest_shouldReturnBadRequestErrorPayload() {
        ServletWebRequest request = WebRequests.mockServletRequest("/api/auth/reset-password", "header-req-4");

        ResponseEntity<ApiError> response = authExceptionHandler.handleBadRequest(
                new BadRequestException("Password must be at least 8 characters"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.BAD_REQUEST.code(), response.getBody().code());
        assertEquals(
                "Password must be at least 8 characters", response.getBody().message());
        assertEquals("/api/auth/reset-password", response.getBody().error().path());
        assertEquals("header-req-4", response.getBody().requestId());
    }

    @Test
    void handlers_shouldPreferMdcRequestIdOverHeaderValue() {
        MDC.put(RequestIdFilter.MDC_KEY, "mdc-id-1");
        ServletWebRequest request = WebRequests.mockServletRequest("/api/auth/login", "header-ignored");

        ResponseEntity<ApiError> response =
                authExceptionHandler.handleBadCredentials(new BadCredentialsException("bad credentials"), request);

        assertEquals("mdc-id-1", response.getBody().requestId());
    }

    @Test
    void handlers_shouldFallbackToNullWhenNoMdcAndNoHeaderRequestId() {
        ServletWebRequest request = WebRequests.mockServletRequest("/api/auth/login", null);

        ResponseEntity<ApiError> response =
                authExceptionHandler.handleUnauthorized(new UnauthorizedException("missing request id"), request);

        assertNull(response.getBody().requestId());
    }
}
