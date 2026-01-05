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
 * Global exception handler for all REST controllers
 * Converts exceptions to standardized ApiError responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handle validation errors (from @Valid annotation)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                List<String> details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.toList());

                ApiError error = ApiError.builder()
                                .code("VALIDATION_ERROR")
                                .message("Invalid request parameters")
                                .path(getPath(request))
                                .details(details)
                                .build();

                log.warn("Validation error on {}: {}", getPath(request), details);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        /**
         * Handle JSON parsing errors
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex,
                        WebRequest request) {

                String message = "Malformed JSON request";
                List<String> details = Collections.emptyList();

                Throwable cause = ex.getCause();
                if (cause instanceof InvalidFormatException ife) {
                        String fieldName = ife.getPath().stream()
                                        .map(JsonMappingException.Reference::getFieldName)
                                        .collect(Collectors.joining("."));
                        message = "Invalid format for field: " + fieldName;
                        details = List.of("Expected type: " + ife.getTargetType().getSimpleName());
                } else if (cause instanceof MismatchedInputException mie) {
                        String fieldName = mie.getPath().stream()
                                        .map(JsonMappingException.Reference::getFieldName)
                                        .collect(Collectors.joining("."));
                        message = "Mismatched input for field: " + fieldName;
                        details = List.of("Check the data type of the field");
                }

                ApiError error = ApiError.builder()
                                .code("BAD_REQUEST")
                                .message(message)
                                .path(getPath(request))
                                .details(details)
                                .build();

                log.warn("JSON parsing error on {}: {}", getPath(request), ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        /**
         * Handle business exception (polymorphic handling)
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusinessException(
                        BusinessException ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .code(ex.getErrorCode())
                                .message(ex.getMessage())
                                .path(getPath(request))
                                .details(Collections.emptyList())
                                .build();

                log.warn("Business exception on {}: {} - {}",
                                getPath(request), ex.getErrorCode(), ex.getMessage());

                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(error);
        }

        /**
         * Handle all other unexpected exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(
                        Exception ex,
                        WebRequest request) {

                log.error("Unexpected error on {}", getPath(request), ex);

                ApiError error = ApiError.builder()
                                .code("INTERNAL_ERROR")
                                .message("An unexpected error occurred")
                                .path(getPath(request))
                                .details(Collections.emptyList())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }

        private String getPath(WebRequest request) {
                if (request instanceof ServletWebRequest swr) {
                        return swr.getRequest().getRequestURI();
                }
                return "unknown";
        }
}
