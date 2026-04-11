package com.cimhub.notificationservice.service;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketNotificationPublisher webSocketNotificationPublisher;

    @BeforeEach
    void setUp() {
        webSocketNotificationPublisher = new WebSocketNotificationPublisher(messagingTemplate);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user-1", "12345", "user+alias@example.com"})
    void publish_shouldSendNotificationToUserTopic(String recipientId) {
        NotificationResponse notification = NotificationResponse.builder()
                .notificationId("n-1")
                .recipientId(recipientId)
                .senderId("sender-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId("booking-1")
                .metadata(Map.of("tripId", "trip-1"))
                .read(false)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();

        webSocketNotificationPublisher.publish(notification);

        verify(messagingTemplate).convertAndSend("/topic/notifications/" + recipientId, notification);
    }

    @Test
    void publishUnreadCount_shouldSendStatsToUserStatsTopicWithExactPayload() {
        webSocketNotificationPublisher.publishUnreadCount("user-2", 7L);

        verify(messagingTemplate).convertAndSend("/topic/notifications/user-2/stats", Map.of("unreadCount", 7L));
    }

    @Test
    void publish_whenTemplateThrows_shouldPropagateException() {
        NotificationResponse notification = NotificationResponse.builder()
                .notificationId("n-err")
                .recipientId("user-1")
                .senderId("sender-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId("booking-1")
                .metadata(Map.of())
                .read(false)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();

        doThrow(new RuntimeException("ws down")).when(messagingTemplate)
                .convertAndSend(any(String.class), any(NotificationResponse.class));

        assertThrows(RuntimeException.class, () -> webSocketNotificationPublisher.publish(notification));
    }

    @Test
    void publish_differentNotificationTypesShouldUseSameUserTopic() {
        NotificationResponse bookingRequested = NotificationResponse.builder()
                .notificationId("n-1")
                .recipientId("user-3")
                .senderId("sender-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId("booking-1")
                .metadata(Map.of())
                .read(false)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();
        NotificationResponse bookingConfirmed = NotificationResponse.builder()
                .notificationId("n-2")
                .recipientId("user-3")
                .senderId("sender-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_CONFIRMED)
                .relatedEntityId("booking-2")
                .metadata(Map.of())
                .read(false)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();

        webSocketNotificationPublisher.publish(bookingRequested);
        webSocketNotificationPublisher.publish(bookingConfirmed);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/user-3"), eq(bookingRequested));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/user-3"), eq(bookingConfirmed));
    }
}
