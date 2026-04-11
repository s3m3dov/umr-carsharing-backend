package com.cimhub.notificationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import com.cimhub.notificationservice.model.Notification;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationMapperTest {

    private final NotificationMapper notificationMapper = new NotificationMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        Notification notification = Notification.builder()
                .notificationId("not-1")
                .recipientId("recipient-1")
                .senderId("sender-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_CONFIRMED)
                .relatedEntityId("booking-1")
                .metadata(Map.of("tripId", "trip-1"))
                .read(true)
                .readAt(Instant.parse("2030-01-01T00:01:00Z"))
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:02:00Z"))
                .build();

        NotificationResponse response = notificationMapper.toResponse(notification);

        assertEquals("not-1", response.notificationId());
        assertEquals("recipient-1", response.recipientId());
        assertEquals("sender-1", response.senderId());
        assertEquals("Title", response.title());
        assertEquals("Message", response.message());
        assertEquals(NotificationType.BOOKING_CONFIRMED, response.type());
        assertEquals("booking-1", response.relatedEntityId());
        assertEquals(Map.of("tripId", "trip-1"), response.metadata());
        assertEquals(true, response.read());
        assertEquals(Instant.parse("2030-01-01T00:01:00Z"), response.readAt());
        assertEquals(Instant.parse("2030-01-01T00:00:00Z"), response.createdAt());
        assertEquals(Instant.parse("2030-01-01T00:02:00Z"), response.updatedAt());
    }
}
