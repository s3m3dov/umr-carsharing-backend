package com.cimhub.notificationservice.service;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Map;

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

    @Test
    void publish_shouldSendNotificationToUserTopic() {
        NotificationResponse notification = NotificationResponse.builder()
                .notificationId("n-1")
                .recipientId("user-1")
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

        verify(messagingTemplate).convertAndSend("/topic/notifications/user-1", notification);
    }

    @Test
    void publishUnreadCount_shouldSendStatsToUserStatsTopic() {
        webSocketNotificationPublisher.publishUnreadCount("user-2", 7L);

        verify(messagingTemplate).convertAndSend("/topic/notifications/user-2/stats", Map.of("unreadCount", 7L));
    }
}
