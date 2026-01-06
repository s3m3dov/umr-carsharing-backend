package com.cimhub.common.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ErrorDetail;
import com.cimhub.common.filter.RequestIdFilter;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        private static final String TRACE_ID_KEY = RequestIdFilter.MDC_KEY;

        // --- ResponseEntityExceptionHandler Overrides ---

        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {

                List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> ErrorDetail.of(
                                                error.getField(),
                                                error.getDefaultMessage(),
                                                ErrorType.VALIDATION_ERROR.name()))
                                .toList();

                ApiError apiError = buildApiError(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                ErrorType.VALIDATION_ERROR.name(),
                                "Validation failed",
                                request,
                                errors);

                return handleExceptionInternal(ex, apiError, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
        }

        @Override
        protected ResponseEntity<Object> handleExceptionInternal(
                        Exception ex,
                        @Nullable Object body,
                        HttpHeaders headers,
                        HttpStatusCode statusCode,
                        WebRequest request) {

                if (body instanceof ApiError) {
                        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
                }

                // Fallback for standard Spring exceptions that we haven't explicitly handled
                // If body is ProblemDetail (default in Spring 6), we can extract from it, or
                // just use defaults
                String message = "An unexpected error occurred";
                String code = ErrorCode.INTERNAL_SERVER_ERROR.code();

                if (body instanceof ProblemDetail problemDetail) {
                        message = problemDetail.getDetail(); // This is usually safe for standard Spring exceptions
                                                             // (e.g. "Method Not Allowed")
                        // Try to find if there is a 'code' property, otherwise map status to error code
                        Object codeProp = problemDetail.getProperties() != null
                                        ? problemDetail.getProperties().get("code")
                                        : null;
                        if (codeProp instanceof String c) {
                                code = c;
                        }
                }

                // Sanitize message for generic exceptions if it wasn't a ProblemDetail or if
                // it's too raw
                if (!(ex instanceof ErrorResponseException) && statusCode.is5xxServerError()) {
                        message = "An unexpected internal error occurred";
                }

                ApiError apiError = buildApiError(
                                statusCode instanceof HttpStatus ? (HttpStatus) statusCode
                                                : HttpStatus.valueOf(statusCode.value()),
                                code,
                                message != null ? message : "An unexpected error occurred",
                                request,
                                Collections.emptyList());

                return super.handleExceptionInternal(ex, apiError, headers, statusCode, request);
        }

        // --- Custom Exception Handlers ---

        @ExceptionHandler(ErrorResponseException.class)
        public ResponseEntity<Object> handleErrorResponseException(ErrorResponseException ex, WebRequest request) {
                ProblemDetail problemDetail = ex.getBody();

                String code = (String) problemDetail.getProperties().getOrDefault("code", "UNKNOWN_ERROR");

                // Safely cast details, filtering out non-ErrorDetail objects if any (though
                // BusinessException enforces it now)
                List<ErrorDetail> details = Collections.emptyList();
                if (problemDetail.getProperties().get("details") instanceof List<?> rawDetails) {
                        details = rawDetails.stream()
                                        .filter(item -> item instanceof ErrorDetail)
                                        .map(item -> (ErrorDetail) item)
                                        .toList();
                }

                ApiError apiError = buildApiError(
                                HttpStatus.valueOf(problemDetail.getStatus()),
                                code,
                                problemDetail.getDetail(),
                                request,
                                details);

                return createResponseEntity(apiError, null, HttpStatus.valueOf(problemDetail.getStatus()), request);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
                List<ErrorDetail> errors = ex.getConstraintViolations().stream()
                                .map(violation -> ErrorDetail.of(
                                                violation.getPropertyPath().toString(),
                                                violation.getMessage(),
                                                ErrorType.CONSTRAINT_VIOLATION.name()))
                                .toList();

                ApiError apiError = buildApiError(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                ErrorType.VALIDATION_ERROR.name(),
                                "Validation failed",
                                request,
                                errors);

                return createResponseEntity(apiError, null, HttpStatus.UNPROCESSABLE_ENTITY, request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
                // Sanitize message: do NOT expose ex.getMessage()
                log.error("Unexpected error occurred", ex);

                ApiError apiError = buildApiError(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ErrorCode.INTERNAL_SERVER_ERROR.code(),
                                "An unexpected internal error occurred", // Sanitized
                                request,
                                Collections.emptyList());

                return createResponseEntity(apiError, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }

        // --- Helpers ---

        private ApiError buildApiError(
                        HttpStatus status,
                        String code,
                        String message,
                        WebRequest request,
                        List<ErrorDetail> details) {

                String path = request.getContextPath(); // WebRequest doesn't give full path easily like
                                                        // HttpServletRequest
                // casting to get full path if possible
                if (request instanceof org.springframework.web.context.request.ServletWebRequest servletRequest) {
                        path = servletRequest.getRequest().getRequestURI();
                        String query = servletRequest.getRequest().getQueryString();
                        if (query != null) {
                                path += "?" + query;
                        }
                } else {
                        path = request.getDescription(false).replace("uri=", "");
                }

                // Try to get requestId from MDC
                String requestId = MDC.get(TRACE_ID_KEY);
                if (requestId == null) {
                        // Try to get from header
                        requestId = request.getHeader("X-Request-Id");
                }

                return ApiError.builder()
                                .status(status.value())
                                .code(code)
                                .message(message)
                                .path(path)
                                .details(details != null ? details : Collections.emptyList())
                                .timestamp(Instant.now())
                                .requestId(requestId)
                                .build();
        }
}
