package com.cimhub.common.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.filter.RequestIdFilter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private jakarta.servlet.http.HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @AfterEach
    void tearDown() {
        MDC.remove(RequestIdFilter.MDC_KEY);
    }

    @Test
    void handleBusinessException_shouldMapToApiError() {
        ServletWebRequest request = buildWebRequest("/api/test", null, "req-100");
        ResourceNotFoundException ex = new ResourceNotFoundException("Trip missing");

        ResponseEntity<Object> response = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.code(), error.code());
        assertEquals("Trip missing", error.message());
        assertEquals("/api/test", error.error().path());
        assertEquals("req-100", error.requestId());
    }

    @Test
    void handleConstraintViolation_shouldReturnUnprocessableEntity() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        Path path = org.mockito.Mockito.mock(Path.class);
        when(path.toString()).thenReturn("requestedSeats");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ServletWebRequest request = buildWebRequest("/api/book", null, null);

        ResponseEntity<Object> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertEquals("Validation failed", error.message());
        assertEquals(1, error.error().details().size());
        assertEquals("requestedSeats", error.error().details().getFirst().field());
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnValidationDetails() {
        DummyRequest target = new DummyRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.rejectValue("email", "NotBlank", "must not be blank");

        MethodParameter parameter = buildDummyMethodParameter();
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ServletWebRequest request = buildWebRequest("/api/users", "page=1", null);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex,
                new HttpHeaders(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertEquals("/api/users?page=1", error.error().path());
        assertEquals(1, error.error().details().size());
        assertEquals("email", error.error().details().getFirst().field());
    }

    @Test
    void handleHttpMessageNotReadable_shouldMapDeserializeMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "read failed",
                new RuntimeException("Cannot deserialize value"),
                null);
        ServletWebRequest request = buildWebRequest("/api/trips", null, null);

        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(
                ex,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertEquals("Invalid data format in request body", error.message());
        assertEquals(ErrorCode.BAD_REQUEST.code(), error.code());
    }

    @Test
    void handleAllUncaughtException_shouldReturnInternalServerError() {
        ServletWebRequest request = buildWebRequest("/api/fail", null, null);

        ResponseEntity<Object> response = handler.handleAllUncaughtException(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.code(), error.code());
        assertNotNull(error.timestamp());
    }

    @Test
    void handleBusinessException_shouldUseMdcRequestIdWhenPresent() {
        MDC.put(RequestIdFilter.MDC_KEY, "mdc-req-1");
        ServletWebRequest request = buildWebRequest("/api/secure", null, "header-req");
        ForbiddenException ex = new ForbiddenException("Denied");

        ResponseEntity<Object> response = handler.handleBusinessException(ex, request);

        ApiError error = (ApiError) response.getBody();
        assertEquals("mdc-req-1", error.requestId());
    }

    private ServletWebRequest buildWebRequest(String uri, String query, String requestIdHeader) {
        lenient().when(servletRequest.getRequestURI()).thenReturn(uri);
        lenient().when(servletRequest.getQueryString()).thenReturn(query);
        lenient().when(servletRequest.getHeader("X-Request-Id")).thenReturn(requestIdHeader);
        return new ServletWebRequest(servletRequest);
    }

    private MethodParameter buildDummyMethodParameter() {
        try {
            return new MethodParameter(
                    DummyController.class.getDeclaredMethod("dummy", String.class),
                    0);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    private static class DummyController {
        void dummy(String value) {
        }
    }

    @SuppressWarnings("unused")
    private static class DummyRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
