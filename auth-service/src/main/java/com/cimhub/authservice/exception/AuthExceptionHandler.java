package com.cimhub.authservice.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ErrorBody;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ErrorCode;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.filter.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(ErrorCode.UNAUTHORIZED.code())
                .message("Invalid credentials")
                .error(ErrorBody.of(resolvePath(request)))
                .timestamp(Instant.now())
                .requestId(resolveRequestId(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        log.warn("Unauthorized: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(ErrorCode.UNAUTHORIZED.code())
                .message(ex.getMessage())
                .error(ErrorBody.of(resolvePath(request)))
                .timestamp(Instant.now())
                .requestId(resolveRequestId(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        log.warn("User already exists: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .code(ErrorCode.USER_ALREADY_EXISTS.code())
                .message(ex.getMessage())
                .error(ErrorBody.of(resolvePath(request)))
                .timestamp(Instant.now())
                .requestId(resolveRequestId(request))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.BAD_REQUEST.code())
                .message(ex.getMessage())
                .error(ErrorBody.of(resolvePath(request)))
                .timestamp(Instant.now())
                .requestId(resolveRequestId(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    private String resolvePath(WebRequest request) {
        if (request instanceof org.springframework.web.context.request.ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }

    private String resolveRequestId(WebRequest request) {
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        return requestId != null ? requestId : request.getHeader("X-Request-Id");
    }
}
