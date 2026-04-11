package com.cimhub.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cimhub.common.dto.response.ApiError;
import org.junit.jupiter.api.Test;

class ApiResponseWrapperTest {

    @Test
    void success_withData_shouldPopulateDataAndDefaultMessage() {
        ApiResponseWrapper<Integer> response = ApiResponseWrapper.success(123);

        assertEquals(123, response.getData());
        assertEquals("Operation successful", response.getMessage());
        assertNotNull(response.getTimestamp());
        assertNull(response.getError());
    }

    @Test
    void success_withMessageOnly_shouldPopulateMessage() {
        ApiResponseWrapper<String> response = ApiResponseWrapper.success("Created");

        assertEquals("Created", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_withDataAndMessage_shouldPopulateBoth() {
        ApiResponseWrapper<Integer> response = ApiResponseWrapper.success(42, "Done");

        assertEquals(42, response.getData());
        assertEquals("Done", response.getMessage());
    }

    @Test
    void error_withApiError_shouldPopulateError() {
        ApiError apiError = ApiError.builder().code("ERR").message("Failed").build();

        ApiResponseWrapper<Object> response = ApiResponseWrapper.error(apiError);

        assertEquals("ERR", response.getError().code());
        assertEquals("An error occurred", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_withCodeAndMessage_shouldCreateApiError() {
        ApiResponseWrapper<Object> response = ApiResponseWrapper.error("BAD_REQUEST", "Invalid input");

        assertEquals("BAD_REQUEST", response.getError().code());
        assertEquals("Invalid input", response.getError().message());
    }

    @Test
    void error_withCodeMessageAndPath_shouldSetPath() {
        ApiResponseWrapper<Object> response = ApiResponseWrapper.error("BAD_REQUEST", "Invalid input", "/api/test");

        assertEquals("/api/test", response.getError().error().path());
    }
}
