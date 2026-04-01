package com.cimhub.notificationservice.service;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.notificationservice.dto.request.CreateNotificationRequest;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import com.cimhub.notificationservice.dto.response.NotificationStatsResponse;
import com.cimhub.notificationservice.exception.InvalidNotificationSenderException;
import com.cimhub.notificationservice.exception.UnauthorizedNotificationAccessException;
import com.cimhub.notificationservice.model.Notification;
import com.cimhub.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RealtimeNotificationPublisher realtimePublisher;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, new NotificationMapper(), realtimePublisher);
    }

    @Test
    void createNotification_withSameSenderAndRecipient_shouldThrow() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId("user-1")
                .senderId("user-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .build();

        assertThrows(InvalidNotificationSenderException.class, () -> notificationService.createNotification(request));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldPersistAndPublishRealtimeUpdates() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId("driver-1")
                .senderId("passenger-1")
                .title("Booking requested")
                .message("Passenger requested seats")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId("booking-1")
                .metadata(Map.of("tripId", "trip-1"))
                .build();

        Notification saved = Notification.builder()
                .notificationId("not-1")
                .recipientId("driver-1")
                .senderId("passenger-1")
                .title("Booking requested")
                .message("Passenger requested seats")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId("booking-1")
                .metadata(Map.of("tripId", "trip-1"))
                .read(false)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
        when(notificationRepository.countByRecipientIdAndReadFalse("driver-1")).thenReturn(3L);

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals("not-1", response.notificationId());
        assertEquals("driver-1", response.recipientId());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("driver-1", captor.getValue().getRecipientId());
        assertEquals("passenger-1", captor.getValue().getSenderId());

        verify(realtimePublisher).publish(response);
        verify(realtimePublisher).publishUnreadCount("driver-1", 3L);
    }

    @Test
    void markAsRead_whenDifferentRecipient_shouldThrowUnauthorized() {
        Notification notification = Notification.builder()
                .notificationId("not-1")
                .recipientId("owner-user")
                .build();

        when(notificationRepository.findById("not-1")).thenReturn(Optional.of(notification));

        assertThrows(
                UnauthorizedNotificationAccessException.class,
                () -> notificationService.markAsRead("not-1", "another-user"));
    }

    @Test
    void markAllAsRead_shouldUpdateUnreadNotificationsAndReturnZeroStats() {
        Notification first = Notification.builder()
                .notificationId("n-1")
                .recipientId("user-1")
                .read(false)
                .build();
        Notification second = Notification.builder()
                .notificationId("n-2")
                .recipientId("user-1")
                .read(false)
                .build();

        when(notificationRepository.findByRecipientIdAndReadFalse("user-1"))
                .thenReturn(List.of(first, second));

        NotificationStatsResponse response = notificationService.markAllAsRead("user-1");

        assertEquals("user-1", response.recipientId());
        assertEquals(0L, response.unreadCount());
        verify(notificationRepository).saveAll(any(List.class));
        verify(realtimePublisher).publishUnreadCount("user-1", 0L);
    }
}
