package com.cimhub.notificationservice.service;

import com.cimhub.notificationservice.dto.response.NotificationResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationPublisher implements RealtimeNotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(NotificationResponse notification) {
        messagingTemplate.convertAndSend(userTopic(notification.recipientId()), notification);
    }

    @Override
    public void publishUnreadCount(String recipientId, long unreadCount) {
        messagingTemplate.convertAndSend(userStatsTopic(recipientId), Map.of("unreadCount", unreadCount));
    }

    private String userTopic(String userId) {
        return "/topic/notifications/" + userId;
    }

    private String userStatsTopic(String userId) {
        return "/topic/notifications/" + userId + "/stats";
    }
}
