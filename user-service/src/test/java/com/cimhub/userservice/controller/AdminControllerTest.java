package com.cimhub.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.userservice.dto.response.AdminProfileResponse;
import org.junit.jupiter.api.Test;

class AdminControllerTest {

    private final AdminController adminController = new AdminController();

    @Test
    void getAdminProfile_withAdminHeaders_shouldReturnProfile() {
        var response = adminController.getAdminProfile(UserType.ADMIN, "admin@example.com");

        ApiResponseWrapper<AdminProfileResponse> body = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("admin@example.com", body.getData().adminEmail());
        assertEquals(UserType.ADMIN, body.getData().role());
    }

    @Test
    void getAdminProfile_withBlankEmail_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> adminController.getAdminProfile(UserType.ADMIN, "   "));
    }

    @Test
    void getAdminProfile_withNonAdminRole_shouldThrowForbidden() {
        assertThrows(
                ForbiddenException.class,
                () -> adminController.getAdminProfile(UserType.PASSENGER, "admin@example.com"));
    }
}
