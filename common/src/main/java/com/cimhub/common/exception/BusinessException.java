package com.cimhub.common.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import java.util.List;

/**
 * Base exception for business logic errors.
 * Extends ErrorResponseException to leverage Spring's ProblemDetail mechanism.
 */
public abstract class BusinessException extends ErrorResponseException {

    private final ServiceErrorCode errorCode;

    /**
     * Create a business exception with ServiceErrorCode and optional custom
     * message.
     */
    protected BusinessException(ServiceErrorCode errorCode, String message, String requestPath) {
        this(errorCode, message, requestPath, null);
    }

    /**
     * Create a business exception with ServiceErrorCode, message, and details.
     */
    protected BusinessException(ServiceErrorCode errorCode, String message, String requestPath, List<Object> details) {
        super(HttpStatusCode.valueOf(errorCode.getHttpStatus().value()),
                createProblemDetail(errorCode, message, requestPath, details), null);
        this.errorCode = errorCode;
    }

    private static ProblemDetail createProblemDetail(ServiceErrorCode errorCode, String message, String requestPath,
            List<Object> details) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(errorCode.getHttpStatus());
        problemDetail.setDetail(message != null ? message : errorCode.getDefaultMessage());
        if (requestPath != null) {
            problemDetail.setInstance(URI.create(requestPath));
        }
        problemDetail.setProperty("code", errorCode.code());
        if (details != null && !details.isEmpty()) {
            problemDetail.setProperty("details", details);
        }
        return problemDetail;
    }

    public ServiceErrorCode getErrorCode() {
        return errorCode;
    }
}
