package com.cimhub.notificationservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(name = "MarkNotificationReadRequest", description = "Request to mark a notification as read")
public record MarkNotificationReadRequest(
        @NotBlank(message = "recipientId is required")
        @Schema(description = "Notification owner user ID", example = "jane.passenger@example.com")
        String recipientId) {
}
