package com.cimhub.notificationservice.dto.request;

import com.cimhub.common.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Map;

@Builder
@Schema(name = "CreateNotificationRequest", description = "Request to create and deliver a notification")
public record CreateNotificationRequest(
        @NotBlank(message = "recipientId is required")
        @Schema(description = "Notification recipient user ID", example = "jane.passenger@example.com")
        String recipientId,

        @Schema(description = "Notification sender user ID", example = "john.driver@example.com")
        String senderId,

        @NotBlank(message = "title is required")
        @Schema(description = "Notification title", example = "Booking Confirmed")
        String title,

        @NotBlank(message = "message is required")
        @Schema(description = "Notification message body", example = "Your booking for trip trip_001 is confirmed.")
        String message,

        @NotNull(message = "type is required")
        @Schema(description = "Notification type", example = "BOOKING_REQUESTED")
        NotificationType type,

        @Schema(description = "Related entity id such as trip or booking", example = "booking_001")
        String relatedEntityId,

        @Schema(description = "Additional metadata payload")
        Map<String, Object> metadata) {
}
