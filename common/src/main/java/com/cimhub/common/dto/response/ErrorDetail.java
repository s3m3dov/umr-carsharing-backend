package com.cimhub.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Structured error detail for validation and business errors.
 * Provides field-level error information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(String field, String message, String type, Map<String, Object> context) {

    public static ErrorDetail of(String message) {
        return new ErrorDetail(null, message, null, null);
    }

    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message, null, null);
    }

    public static ErrorDetail of(String field, String message, String type) {
        return new ErrorDetail(field, message, type, null);
    }

    public static ErrorDetail of(String field, String message, String type, Map<String, Object> context) {
        return new ErrorDetail(field, message, type, context);
    }
}
