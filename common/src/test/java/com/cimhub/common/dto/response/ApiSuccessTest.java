package com.cimhub.common.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ApiSuccessTest {

    @Test
    void success_withData_shouldPopulateDataAndDefaultMessage() {
        ApiSuccess<Integer> response = ApiSuccess.success(123);

        assertEquals(123, response.getData());
        assertEquals("Operation successful", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_withMessageOnly_shouldPopulateMessage() {
        ApiSuccess<String> response = ApiSuccess.success("Created");

        assertEquals("Created", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_withDataAndMessage_shouldPopulateBoth() {
        ApiSuccess<Integer> response = ApiSuccess.success(42, "Done");

        assertEquals(42, response.getData());
        assertEquals("Done", response.getMessage());
    }
}
