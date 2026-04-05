package com.cimhub.common.dto.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiErrorTest {

    @Test
    void of_shouldCreateErrorWithPathAndTimestamp() {
        ApiError error = ApiError.of(400, "BAD_REQUEST", "Invalid request", "/api/test");

        assertEquals(400, error.status());
        assertEquals("BAD_REQUEST", error.code());
        assertEquals("Invalid request", error.message());
        assertEquals("/api/test", error.error().path());
        assertNotNull(error.timestamp());
    }

    @Test
    void withDetails_shouldIncludeProvidedDetails() {
        ErrorDetail detail = ErrorDetail.of("field", "must not be blank", "VALIDATION_ERROR");
        ApiError error = ApiError.withDetails(422, "VALIDATION_ERROR", "Validation failed", "/api/validate", List.of(detail));

        assertEquals(422, error.status());
        assertEquals(1, error.error().details().size());
        assertEquals("field", error.error().details().getFirst().field());
    }

    @Test
    void withRequestId_shouldPopulateRequestId() {
        ApiError error = ApiError.withRequestId(401, "UNAUTHORIZED", "Auth required", "/api/auth", "req-1");

        assertEquals("req-1", error.requestId());
        assertEquals("/api/auth", error.error().path());
    }

    @Test
    void withRequestIdAndDetails_shouldPopulateBoth() {
        ErrorDetail detail = ErrorDetail.of("token", "expired", "AUTH_ERROR");
        ApiError error = ApiError.withRequestIdAndDetails(
                401,
                "UNAUTHORIZED",
                "Auth failed",
                "/api/auth",
                List.of(detail),
                "req-2");

        assertEquals("req-2", error.requestId());
        assertEquals(1, error.error().details().size());
        assertEquals("token", error.error().details().getFirst().field());
    }
}
