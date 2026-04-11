package com.cimhub.userservice.controller;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void healthCheck_shouldReturnSuccessMessage() {
        var response = userController.healthCheck();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User Service is up and running!", response.getBody().getMessage());
    }

    @Test
    void getUserInfo_shouldReturnWrappedResponse() {
        UserResponse userResponse = new UserResponse(
                "driver@example.com",
                "John",
                "Doe",
                "driver@example.com",
                UserType.DRIVER,
                "+123",
                30,
                "LIC-123",
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:05:00Z"));
        when(userService.getUserInfo("driver@example.com")).thenReturn(userResponse);

        var response = userController.getUserInfo("driver@example.com", "driver@example.com");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("driver@example.com", response.getBody().getData().userId());
        assertEquals(UserType.DRIVER, response.getBody().getData().userType());
    }

    @Test
    void getUserInfo_withMismatchedOwner_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> userController.getUserInfo("other@example.com", "driver@example.com"));
    }

    @Test
    void getUserInfo_adminAccessingOtherUser_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> userController.getUserInfo("admin@example.com", "driver@example.com"));
    }
}
