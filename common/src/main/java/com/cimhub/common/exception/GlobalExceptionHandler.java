package com.cimhub.common.exception;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ErrorBody;
import com.cimhub.common.dto.response.ErrorDetail;
import com.cimhub.common.filter.RequestIdFilter;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TRACE_ID_KEY = RequestIdFilter.MDC_KEY;

    // --- ResponseEntityExceptionHandler Overrides ---

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        return createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorType.VALIDATION_ERROR.name(),
                "Validation failed",
                errors,
                request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.warn("Failed to read request body: {}", ex.getMessage());

        String message = "Invalid request format";
        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage != null) {
                // Extract meaningful error info from Jackson exceptions
                if (causeMessage.contains("Cannot deserialize")) {
                    message = "Invalid data format in request body";
                } else if (causeMessage.contains("required") || causeMessage.contains("missing")) {
                    message = "Missing required fields in request body";
                } else if (causeMessage.contains("not a valid")) {
                    message = "Invalid value provided in request body";
                }
            }
        }

        return createErrorResponse(
                HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST.code(), message, Collections.emptyList(), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        if (body instanceof ApiError) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }

        return createGenericErrorResponse(ex, body, statusCode, request);
    }

    // --- Custom Exception Handlers ---

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(this::mapConstraintViolation)
                .toList();

        return createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorType.VALIDATION_ERROR.name(),
                "Validation failed",
                errors,
                request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
        return createErrorResponse(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().code(),
                ex.getMessage(),
                Collections.emptyList(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.code(),
                "An unexpected internal error occurred",
                Collections.emptyList(),
                request);
    }

    // --- Helpers ---

    private ResponseEntity<Object> createGenericErrorResponse(
            Exception ex, Object body, HttpStatusCode statusCode, WebRequest request) {
        String message = "An unexpected error occurred";
        String code = ErrorCode.INTERNAL_SERVER_ERROR.code();
        List<ErrorDetail> details = Collections.emptyList();

        if (body instanceof ProblemDetail problemDetail) {
            message = problemDetail.getDetail();

            Object codeProp = problemDetail.getProperties() != null
                    ? problemDetail.getProperties().get("code")
                    : null;
            if (codeProp instanceof String c) {
                code = c;
            }

            if (problemDetail.getProperties() != null
                    && problemDetail.getProperties().get("details") instanceof List<?> rawDetails) {
                details = rawDetails.stream()
                        .filter(item -> item instanceof ErrorDetail)
                        .map(item -> (ErrorDetail) item)
                        .toList();
            }
        } else if (!(ex instanceof ErrorResponseException) && statusCode.is5xxServerError()) {
            message = "An unexpected internal error occurred";
        }

        // Sanitize leftover ProblemDetail strings if any
        if (message != null && message.contains("ProblemDetail[")) {
            message = "An unexpected error occurred";
        }

        return createErrorResponse(
                statusCode, code, message != null ? message : "An unexpected error occurred", details, request);
    }

    private ResponseEntity<Object> createErrorResponse(
            HttpStatusCode status, String code, String message, List<ErrorDetail> details, WebRequest request) {
        ErrorBody errorBody = ErrorBody.of(resolvePath(request), details);

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .error(errorBody)
                .timestamp(Instant.now())
                .requestId(resolveRequestId(request))
                .build();

        return new ResponseEntity<>(ApiResponseWrapper.error(apiError), status);
    }

    private String resolvePath(WebRequest request) {
        if (request instanceof org.springframework.web.context.request.ServletWebRequest servletRequest) {
            String path = servletRequest.getRequest().getRequestURI();
            String query = servletRequest.getRequest().getQueryString();
            return query != null ? path + "?" + query : path;
        }
        return request.getDescription(false).replace("uri=", "");
    }

    private String resolveRequestId(WebRequest request) {
        String requestId = MDC.get(TRACE_ID_KEY);
        return requestId != null ? requestId : request.getHeader("X-Request-Id");
    }

    private ErrorDetail mapFieldError(org.springframework.validation.FieldError error) {
        return ErrorDetail.of(error.getField(), error.getDefaultMessage(), ErrorType.VALIDATION_ERROR.name());
    }

    private ErrorDetail mapConstraintViolation(jakarta.validation.ConstraintViolation<?> violation) {
        return ErrorDetail.of(
                violation.getPropertyPath().toString(), violation.getMessage(), ErrorType.CONSTRAINT_VIOLATION.name());
    }
}
