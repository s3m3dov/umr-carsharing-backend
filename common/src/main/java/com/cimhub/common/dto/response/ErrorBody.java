package com.cimhub.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import java.util.List;

/**
 * Represents the nested error object in API error responses.
 * Contains the request path and structured error details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(String path, List<ErrorDetail> details) {

    /**
     * Create an error body with just a path (empty details).
     */
    public static ErrorBody of(String path) {
        return new ErrorBody(path, Collections.emptyList());
    }

    /**
     * Create an error body with path and details.
     */
    public static ErrorBody of(String path, List<ErrorDetail> details) {
        return new ErrorBody(path, details != null ? details : Collections.emptyList());
    }
}
