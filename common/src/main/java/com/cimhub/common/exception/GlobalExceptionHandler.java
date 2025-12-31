package com.cimhub.common.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers
 * Converts exceptions to standardized ApiResponse<Void> with ApiError
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handle validation errors (from @Valid annotation)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                List<String> details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.toList());

                ApiError error = ApiError.builder()
                                .code("VALIDATION_ERROR")
                                .message("Invalid request parameters")
                                .path(request.getRequestURI())
                                .details(details)
                                .build();

                log.warn("Validation error on {}: {}", request.getRequestURI(), details);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(error));
        }

        /**
         * Handle business exception (custom base exception)
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(
                        BusinessException ex,
                        HttpServletRequest request) {

                ApiError error = ApiError.builder()
                                .code(ex.getErrorCode())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Business exception on {}: {} - {}",
                                request.getRequestURI(), ex.getErrorCode(), ex.getMessage());

                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(ApiResponse.error(error));
        }

        /**
         * Handle illegal argument exceptions
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                ApiError error = ApiError.builder()
                                .code("INVALID_ARGUMENT")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Invalid argument on {}: {}", request.getRequestURI(), ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(error));
        }

        /**
         * Handle all other unexpected exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unexpected error on {}", request.getRequestURI(), ex);

                ApiError error = ApiError.builder()
                                .code("INTERNAL_ERROR")
                                .message("An unexpected error occurred")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(error));
        }
}
