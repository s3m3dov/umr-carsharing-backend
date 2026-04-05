package com.cimhub.notificationservice.service;

import com.cimhub.notificationservice.dto.response.NotificationResponse;

public interface RealtimeNotificationPublisher {

    void publish(NotificationResponse notification);

    void publishUnreadCount(String recipientId, long unreadCount);
}
