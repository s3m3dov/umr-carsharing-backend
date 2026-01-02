package com.cimhub.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void constructor_whenCalledWithAllParameters_thenSetsFieldsCorrectly() {
        // When
        BusinessException exception = new BusinessException(
                "TEST_ERROR",
                "Test error message",
                HttpStatus.BAD_REQUEST);

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("TEST_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Test error message");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void constructor_whenCalledWithoutHttpStatus_thenDefaultsToBadRequest() {
        // When
        BusinessException exception = new BusinessException(
                "TEST_ERROR",
                "Test error message");

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("TEST_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Test error message");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void notFound_whenCalled_thenCreatesNotFoundExceptionWithCorrectFormat() {
        // When
        BusinessException exception = BusinessException.notFound("User", "user123");

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("User with id 'user123' not found");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void alreadyExists_whenCalled_thenCreatesConflictExceptionWithCorrectFormat() {
        // When
        BusinessException exception = BusinessException.alreadyExists("Vehicle", "ABC123");

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_ALREADY_EXISTS");
        assertThat(exception.getMessage()).isEqualTo("Vehicle with ABC123 already exists");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void unauthorized_whenCalled_thenCreatesUnauthorizedExceptionWithMessage() {
        // When
        BusinessException exception = BusinessException.unauthorized("Invalid credentials");

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("UNAUTHORIZED");
        assertThat(exception.getMessage()).isEqualTo("Invalid credentials");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void forbidden_whenCalled_thenCreatesForbiddenExceptionWithMessage() {
        // When
        BusinessException exception = BusinessException.forbidden("Access denied");

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("FORBIDDEN");
        assertThat(exception.getMessage()).isEqualTo("Access denied");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void businessException_whenThrown_thenIsRuntimeException() {
        // Given
        BusinessException exception = new BusinessException("TEST", "Test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void notFound_whenCalledWithDifferentResources_thenFormatsCorrectly() {
        // When
        BusinessException tripException = BusinessException.notFound("Trip", "trip456");
        BusinessException bookingException = BusinessException.notFound("Booking", "booking789");

        // Then
        assertThat(tripException.getMessage()).contains("Trip").contains("trip456");
        assertThat(bookingException.getMessage()).contains("Booking").contains("booking789");
    }
}
