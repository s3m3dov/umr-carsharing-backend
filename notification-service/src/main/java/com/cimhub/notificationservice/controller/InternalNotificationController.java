package com.cimhub.notificationservice.controller;

import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for user management in notification-service.
 * Hidden from public API documentation.
 */
@Slf4j
@Hidden
@RestController
@RequestMapping("/api/internal/notifications/users")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Internal: Delete user data",
            description = "Deletes all user-related data from notification-service")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccess<String>> deleteUserData(@PathVariable String userId) {
        log.info("Internal request to delete all notifications for user: {}", userId);
        notificationService.deleteUserData(userId);
        return ResponseEntity.ok(ApiSuccess.success("User notifications deleted from notification-service"));
    }
}
