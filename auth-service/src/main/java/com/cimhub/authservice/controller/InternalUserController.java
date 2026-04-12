package com.cimhub.authservice.controller;

import com.cimhub.authservice.service.AuthService;
import com.cimhub.common.dto.response.ApiSuccess;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for user management.
 * Hidden from public API documentation.
 */
@Slf4j
@Hidden
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;

    @Operation(summary = "Internal: Delete user", description = "Deletes a user account from auth-service")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccess<String>> deleteUser(@PathVariable String userId) {
        log.info("Internal request to delete user: {}", userId);
        authService.deleteUser(userId);
        return ResponseEntity.ok(ApiSuccess.success("User credentials deleted"));
    }
}
