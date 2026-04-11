package com.cimhub.notificationservice.testsupport;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import java.time.Instant;
import java.util.Map;

public final class NotificationFixtures {

    private NotificationFixtures() {}

    public static NotificationResponse notificationResponse(String id, String recipientId, String senderId) {
        return NotificationResponse.builder()
                .notificationId(id)
                .recipientId(recipientId)
                .senderId(senderId)
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId("entity-1")
                .metadata(Map.of("k", "v"))
                .read(false)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();
    }
}
