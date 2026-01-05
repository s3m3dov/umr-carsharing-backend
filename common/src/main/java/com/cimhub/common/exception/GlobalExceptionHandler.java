package com.cimhub.common.exception;

import com.cimhub.common.dto.response.ApiError;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Converts exceptions into standardized ApiError responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("Validation error on {}: {}", getPath(request), details);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Invalid request parameters",
                request,
                details
        );
    }

    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request
    ) {
        String message = "Malformed JSON request";
        List<String> details = Collections.emptyList();

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = extractFieldPath(ife.getPath());
            message = "Invalid format for field: " + field;
            details = List.of("Expected type: " + ife.getTargetType().getSimpleName());
        } else if (cause instanceof MismatchedInputException mie) {
            String field = extractFieldPath(mie.getPath());
            message = "Mismatched input for field: " + field;
            details = List.of("Check the data type of the field");
        }

        log.warn("JSON parsing error on {}: {}", getPath(request), ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                message,
                request,
                details
        );
    }

    /**
     * Handle business exceptions (polymorphic)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            WebRequest request
    ) {
        log.warn("Business exception on {}: {} - {}",
                getPath(request), ex.getErrorCode(), ex.getMessage());

        return buildResponse(
                ex.getHttpStatus(),
                ex.getErrorCode(),
                ex.getMessage(),
                request,
                Collections.emptyList()
        );
    }

    /**
     * Handle unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected error on {}", getPath(request), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                Collections.emptyList()
        );
    }

    /* Helpers */

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            ErrorCode code,
            String message,
            WebRequest request,
            List<String> details
    ) {
        ApiError error = ApiError.builder()
                .code(code.name())
                .message(message)
                .path(getPath(request))
                .details(details)
                .build();

        return ResponseEntity.status(status).body(error);
    }

    private String extractFieldPath(List<JsonMappingException.Reference> path) {
        return path.stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining("."));
    }

    private String getPath(WebRequest request) {
        return request instanceof ServletWebRequest swr
                ? swr.getRequest().getRequestURI()
                : "unknown";
    }
}
