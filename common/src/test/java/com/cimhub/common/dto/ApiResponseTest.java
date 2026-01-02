package com.cimhub.common.dto;

import com.cimhub.common.dto.response.ApiError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void success_whenCalledWithData_thenCreatesSuccessResponse() {
        // Given
        String testData = "Test Data";

        // When
        ApiResponse<String> response = ApiResponse.success(testData);

        // Then
        assertThat(response.getData()).isEqualTo("Test Data");
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).contains("String has been returned");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_whenCalledWithNullData_thenCreatesSuccessResponseWithDefaultMessage() {
        // When
        ApiResponse<String> response = ApiResponse.success(null);

        // Then
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isEqualTo("Operation successful");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_whenCalledWithMessage_thenCreatesSuccessResponseWithMessage() {
        // When
        ApiResponse<Void> response = ApiResponse.success("Custom success message");

        // Then
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isEqualTo("Custom success message");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_whenCalledWithDataAndMessage_thenCreatesSuccessResponseWithBoth() {
        // Given
        Integer testData = 42;

        // When
        ApiResponse<Integer> response = ApiResponse.success(testData, "Data retrieved successfully");

        // Then
        assertThat(response.getData()).isEqualTo(42);
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isEqualTo("Data retrieved successfully");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void error_whenCalledWithApiError_thenCreatesErrorResponse() {
        // Given
        ApiError apiError = ApiError.builder()
                .code("TEST_ERROR")
                .message("Test error message")
                .build();

        // When
        ApiResponse<Void> response = ApiResponse.error(apiError);

        // Then
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isEqualTo(apiError);
        assertThat(response.getMessage()).isEqualTo("An error occurred");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void error_whenCalledWithCodeAndMessage_thenCreatesErrorResponse() {
        // When
        ApiResponse<Void> response = ApiResponse.error("NOT_FOUND", "Resource not found");

        // Then
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getError().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getMessage()).isEqualTo("An error occurred");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void error_whenCalledWithCodeMessageAndPath_thenCreatesErrorResponseWithPath() {
        // When
        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_ERROR",
                "Invalid input",
                "/api/users");

        // Then
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getError().getMessage()).isEqualTo("Invalid input");
        assertThat(response.getError().getPath()).isEqualTo("/api/users");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void builder_whenUsed_thenCreatesCustomResponse() {
        // When
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data("Custom data")
                .message("Custom message")
                .build();

        // Then
        assertThat(response.getData()).isEqualTo("Custom data");
        assertThat(response.getMessage()).isEqualTo("Custom message");
        assertThat(response.getError()).isNull();
    }

    @Test
    void success_whenCalledWithComplexObject_thenIncludesClassNameInMessage() {
        // Given
        TestDto testDto = new TestDto("test", 123);

        // When
        ApiResponse<TestDto> response = ApiResponse.success(testDto);

        // Then
        assertThat(response.getData()).isEqualTo(testDto);
        assertThat(response.getMessage()).contains("TestDto");
    }

    // Helper class for testing
    private static class TestDto {
        private final String name;
        private final int value;

        public TestDto(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TestDto testDto = (TestDto) o;
            return value == testDto.value && name.equals(testDto.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + value;
        }
    }
}
