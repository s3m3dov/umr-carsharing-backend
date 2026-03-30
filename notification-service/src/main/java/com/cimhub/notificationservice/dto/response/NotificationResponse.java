package com.cimhub.notificationservice.dto.response;

import com.cimhub.common.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
@Schema(name = "NotificationResponse", description = "Notification payload returned by API")
public record NotificationResponse(
        @Schema(description = "Notification id", example = "not_001")
        String notificationId,

        @Schema(description = "Notification recipient user id", example = "user_456")
        String recipientId,

        @Schema(description = "Notification sender user id", example = "user_123")
        String senderId,

        @Schema(description = "Notification title", example = "Booking Confirmed")
        String title,

        @Schema(description = "Notification message", example = "Your booking for trip trip_001 is confirmed.")
        String message,

        @Schema(description = "Notification type", example = "BOOKING_REQUESTED")
        NotificationType type,

        @Schema(description = "Related entity id", example = "booking_001")
        String relatedEntityId,

        @Schema(description = "Additional metadata")
        Map<String, Object> metadata,

        @Schema(description = "Whether notification is read", example = "false")
        boolean read,

        @Schema(description = "Read timestamp")
        Instant readAt,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Update timestamp")
        Instant updatedAt) {
}
