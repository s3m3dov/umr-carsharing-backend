package com.cimhub.common.exception;

import com.cimhub.common.dto.response.ErrorDetail;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

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
    protected BusinessException(ServiceErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * Create a business exception with ServiceErrorCode, message, and details.
     */
    protected BusinessException(ServiceErrorCode errorCode, String message, List<ErrorDetail> details) {
        super(
                HttpStatusCode.valueOf(errorCode.getHttpStatus().value()),
                createProblemDetail(errorCode, message, details),
                null);
        this.errorCode = errorCode;
    }

    private static ProblemDetail createProblemDetail(
            ServiceErrorCode errorCode, String message, List<ErrorDetail> details) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(errorCode.getHttpStatus());
        problemDetail.setDetail(message != null ? message : errorCode.getDefaultMessage());

        problemDetail.setProperty("code", errorCode.code());
        if (details != null && !details.isEmpty()) {
            problemDetail.setProperty("details", details);
        }
        return problemDetail;
    }

    public ServiceErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return getBody().getDetail();
    }
}
