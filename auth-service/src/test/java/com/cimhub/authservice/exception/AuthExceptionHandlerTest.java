package com.cimhub.authservice.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ErrorCode;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.filter.RequestIdFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthExceptionHandlerTest {

    private AuthExceptionHandler authExceptionHandler;

    @Mock
    private jakarta.servlet.http.HttpServletRequest servletRequest;

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
        ServletWebRequest request = buildWebRequest("/api/auth/login", "header-req");

        ResponseEntity<ApiError> response = authExceptionHandler.handleBadCredentials(
                new BadCredentialsException("bad credentials"),
                request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED.code(), response.getBody().code());
        assertEquals("Invalid credentials", response.getBody().message());
        assertEquals("/api/auth/login", response.getBody().error().path());
        assertEquals("header-req", response.getBody().requestId());
    }

    @Test
    void handleUnauthorized_shouldReturnUnauthorizedErrorPayload() {
        ServletWebRequest request = buildWebRequest("/api/auth/validate", "header-req-2");

        ResponseEntity<ApiError> response = authExceptionHandler.handleUnauthorized(
                new UnauthorizedException("Invalid or expired token"),
                request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED.code(), response.getBody().code());
        assertEquals("Invalid or expired token", response.getBody().message());
        assertEquals("/api/auth/validate", response.getBody().error().path());
        assertEquals("header-req-2", response.getBody().requestId());
    }

    @Test
    void handleUserAlreadyExists_shouldReturnConflictErrorPayload() {
        ServletWebRequest request = buildWebRequest("/api/auth/signup", "header-req-3");

        ResponseEntity<ApiError> response = authExceptionHandler.handleUserAlreadyExists(
                new UserAlreadyExistsException("User already exists"),
                request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.USER_ALREADY_EXISTS.code(), response.getBody().code());
        assertEquals("User already exists", response.getBody().message());
        assertEquals("/api/auth/signup", response.getBody().error().path());
        assertEquals("header-req-3", response.getBody().requestId());
    }

    @Test
    void handleBadRequest_shouldReturnBadRequestErrorPayload() {
        ServletWebRequest request = buildWebRequest("/api/auth/reset-password", "header-req-4");

        ResponseEntity<ApiError> response = authExceptionHandler.handleBadRequest(
                new BadRequestException("Password must be at least 8 characters"),
                request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.BAD_REQUEST.code(), response.getBody().code());
        assertEquals("Password must be at least 8 characters", response.getBody().message());
        assertEquals("/api/auth/reset-password", response.getBody().error().path());
        assertEquals("header-req-4", response.getBody().requestId());
    }

    @Test
    void handlers_shouldPreferMdcRequestIdOverHeaderValue() {
        MDC.put(RequestIdFilter.MDC_KEY, "mdc-id-1");
        ServletWebRequest request = buildWebRequest("/api/auth/login", "header-ignored");

        ResponseEntity<ApiError> response = authExceptionHandler.handleBadCredentials(
                new BadCredentialsException("bad credentials"),
                request);

        assertEquals("mdc-id-1", response.getBody().requestId());
    }

    private ServletWebRequest buildWebRequest(String uri, String headerRequestId) {
        lenient().when(servletRequest.getRequestURI()).thenReturn(uri);
        lenient().when(servletRequest.getHeader("X-Request-Id")).thenReturn(headerRequestId);
        return new ServletWebRequest(servletRequest);
    }
}
