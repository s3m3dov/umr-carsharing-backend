package com.cimhub.notificationservice.service;

import com.cimhub.notificationservice.dto.request.CreateNotificationRequest;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import com.cimhub.notificationservice.dto.response.NotificationStatsResponse;
import com.cimhub.notificationservice.exception.InvalidNotificationSenderException;
import com.cimhub.notificationservice.exception.NotificationNotFoundException;
import com.cimhub.notificationservice.exception.UnauthorizedNotificationAccessException;
import com.cimhub.notificationservice.model.Notification;
import com.cimhub.notificationservice.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final RealtimeNotificationPublisher realtimePublisher;

    @Transactional
    public void deleteUserData(String userId) {
        log.info("Deleting all notifications for user: {}", userId);
        notificationRepository.deleteByRecipientId(userId);
        notificationRepository.deleteBySenderId(userId);
        log.info("Successfully deleted all notifications for user: {}", userId);
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        if (request.senderId() != null && request.senderId().equals(request.recipientId())) {
            throw new InvalidNotificationSenderException("senderId and recipientId cannot be the same");
        }

        Instant now = Instant.now();
        Notification notification = Notification.builder()
                .recipientId(request.recipientId())
                .senderId(request.senderId())
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .relatedEntityId(request.relatedEntityId())
                .metadata(request.metadata())
                .read(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

        realtimePublisher.publish(response);
        realtimePublisher.publishUnreadCount(
                saved.getRecipientId(), notificationRepository.countByRecipientIdAndReadFalse(saved.getRecipientId()));

        log.info("Notification {} created for recipient {}", saved.getNotificationId(), saved.getRecipientId());
        return response;
    }

    public Page<NotificationResponse> listNotifications(String recipientId, Pageable pageable) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(String notificationId, String recipientId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new UnauthorizedNotificationAccessException("You are not allowed to modify this notification");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification.setUpdatedAt(Instant.now());
            notification = notificationRepository.save(notification);

            realtimePublisher.publishUnreadCount(
                    notification.getRecipientId(),
                    notificationRepository.countByRecipientIdAndReadFalse(notification.getRecipientId()));
        }

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationStatsResponse markAllAsRead(String recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalse(recipientId);
        Instant now = Instant.now();

        unread.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
            notification.setUpdatedAt(now);
        });

        if (!unread.isEmpty()) {
            notificationRepository.saveAll(unread);
        }

        realtimePublisher.publishUnreadCount(recipientId, 0);
        return NotificationStatsResponse.builder()
                .recipientId(recipientId)
                .unreadCount(0)
                .build();
    }

    public NotificationStatsResponse getStats(String recipientId) {
        long unreadCount = notificationRepository.countByRecipientIdAndReadFalse(recipientId);
        return NotificationStatsResponse.builder()
                .recipientId(recipientId)
                .unreadCount(unreadCount)
                .build();
    }
}
