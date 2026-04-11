package com.cimhub.common.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ErrorBodyAndDetailTest {

    @Test
    void errorBody_ofPath_shouldCreateEmptyDetails() {
        ErrorBody body = ErrorBody.of("/api/path");

        assertEquals("/api/path", body.path());
        assertNotNull(body.details());
        assertTrue(body.details().isEmpty());
    }

    @Test
    void errorBody_ofPathAndDetails_shouldUseGivenDetails() {
        ErrorDetail detail = ErrorDetail.of("field", "invalid", "VALIDATION_ERROR");
        ErrorBody body = ErrorBody.of("/api/path", List.of(detail));

        assertEquals(1, body.details().size());
        assertEquals("field", body.details().getFirst().field());
    }

    @Test
    void errorBody_ofPathAndNullDetails_shouldFallbackToEmptyList() {
        ErrorBody body = ErrorBody.of("/api/path", null);

        assertNotNull(body.details());
        assertTrue(body.details().isEmpty());
    }

    @Test
    void errorDetail_factoryMethods_shouldPopulateFields() {
        ErrorDetail d1 = ErrorDetail.of("Message only");
        ErrorDetail d2 = ErrorDetail.of("field", "Bad value");
        ErrorDetail d3 = ErrorDetail.of("field", "Bad value", "VALIDATION_ERROR");
        ErrorDetail d4 = ErrorDetail.of("field", "Bad value", "VALIDATION_ERROR", Map.of("min", 1));

        assertEquals("Message only", d1.message());
        assertEquals("field", d2.field());
        assertEquals("VALIDATION_ERROR", d3.type());
        assertEquals(1, d4.context().get("min"));
    }
}
