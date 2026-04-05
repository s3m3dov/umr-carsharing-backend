package com.cimhub.notificationservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "NotificationStatsResponse", description = "Aggregated notification counters")
public record NotificationStatsResponse(
        @Schema(description = "Recipient user id", example = "jane.passenger@example.com")
        String recipientId,

        @Schema(description = "Unread notification count", example = "3")
        long unreadCount) {
}
