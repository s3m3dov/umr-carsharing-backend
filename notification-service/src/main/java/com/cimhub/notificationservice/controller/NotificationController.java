package com.cimhub.notificationservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.enums.UserType;
import com.cimhub.notificationservice.dto.examples.NotificationApiDoc;
import com.cimhub.notificationservice.dto.request.CreateNotificationRequest;
import com.cimhub.notificationservice.dto.request.MarkNotificationReadRequest;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import com.cimhub.notificationservice.dto.response.NotificationStatsResponse;
import com.cimhub.notificationservice.exception.UnauthorizedNotificationAccessException;
import com.cimhub.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for creating, listing, and managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Create a notification",
            description = "Creates a notification and publishes it over WebSocket")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Notification created",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        value = NotificationApiDoc.CREATE_NOTIFICATION_RESPONSE))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid sender or request payload",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = NotificationApiDoc.ERROR_INVALID_SENDER))),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = NotificationApiDoc.ERROR_UNAUTHORIZED)))
            })
    @PostMapping
    public ResponseEntity<ApiSuccess<NotificationResponse>> createNotification(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Valid @RequestBody CreateNotificationRequest request) {
        validateCreatePermission(authenticatedUserId, authenticatedUserRole, request.senderId());

        CreateNotificationRequest effectiveRequest = request;
        if (StringUtils.hasText(authenticatedUserId) && !StringUtils.hasText(request.senderId())) {
            effectiveRequest = CreateNotificationRequest.builder()
                    .recipientId(request.recipientId())
                    .senderId(authenticatedUserId)
                    .title(request.title())
                    .message(request.message())
                    .type(request.type())
                    .relatedEntityId(request.relatedEntityId())
                    .metadata(request.metadata())
                    .build();
        }

        NotificationResponse created = notificationService.createNotification(effectiveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.success(created));
    }

    @Operation(
            summary = "List recipient notifications",
            description = "Returns paginated notifications ordered by latest first")
    @GetMapping("/{recipientId}")
    public ResponseEntity<ApiSuccess<Page<NotificationResponse>>> listNotifications(
            @Parameter(description = "Recipient user ID", required = true) @PathVariable String recipientId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            Pageable pageable) {
        validateRecipientAccess(recipientId, authenticatedUserId, authenticatedUserRole);
        return ResponseEntity.ok(ApiSuccess.success(notificationService.listNotifications(recipientId, pageable)));
    }

    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Notification updated"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Notification not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(
                                                        value = NotificationApiDoc.ERROR_NOTIFICATION_NOT_FOUND)))
            })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiSuccess<NotificationResponse>> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole,
            @Valid @RequestBody MarkNotificationReadRequest request) {
        validateRecipientAccess(request.recipientId(), authenticatedUserId, authenticatedUserRole);
        NotificationResponse updated = notificationService.markAsRead(notificationId, request.recipientId());
        return ResponseEntity.ok(ApiSuccess.success(updated));
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all unread notifications for a recipient as read")
    @PatchMapping("/{recipientId}/read-all")
    public ResponseEntity<ApiSuccess<NotificationStatsResponse>> markAllAsRead(
            @Parameter(description = "Recipient user ID", required = true) @PathVariable String recipientId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole) {
        validateRecipientAccess(recipientId, authenticatedUserId, authenticatedUserRole);
        return ResponseEntity.ok(ApiSuccess.success(notificationService.markAllAsRead(recipientId)));
    }

    @Operation(summary = "Get notification stats", description = "Returns aggregated notification counters")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Stats loaded",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples =
                                                @ExampleObject(value = NotificationApiDoc.NOTIFICATION_STATS_RESPONSE)))
            })
    @GetMapping("/{recipientId}/stats")
    public ResponseEntity<ApiSuccess<NotificationStatsResponse>> getStats(
            @Parameter(description = "Recipient user ID", required = true) @PathVariable String recipientId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    String authenticatedUserRole) {
        validateRecipientAccess(recipientId, authenticatedUserId, authenticatedUserRole);
        return ResponseEntity.ok(ApiSuccess.success(notificationService.getStats(recipientId)));
    }

    private void validateRecipientAccess(String recipientId, String authenticatedUserId, String authenticatedUserRole) {
        if (!StringUtils.hasText(authenticatedUserId)) {
            throw new UnauthorizedNotificationAccessException("Missing X-User-Id header");
        }

        if (recipientId.equals(authenticatedUserId)) {
            return;
        }

        if (isAdmin(authenticatedUserRole)) {
            return;
        }

        throw new UnauthorizedNotificationAccessException("You are not allowed to access notifications for this user");
    }

    private void validateCreatePermission(String authenticatedUserId, String authenticatedUserRole, String senderId) {
        if (!StringUtils.hasText(authenticatedUserId)) {
            return;
        }

        if (!StringUtils.hasText(senderId) || authenticatedUserId.equals(senderId)) {
            return;
        }

        if (isAdmin(authenticatedUserRole)) {
            return;
        }

        throw new UnauthorizedNotificationAccessException(
                "You are not allowed to create notifications on behalf of another user");
    }

    private boolean isAdmin(String authenticatedUserRole) {
        if (!StringUtils.hasText(authenticatedUserRole)) {
            return false;
        }

        try {
            return UserType.ADMIN == UserType.valueOf(authenticatedUserRole.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
