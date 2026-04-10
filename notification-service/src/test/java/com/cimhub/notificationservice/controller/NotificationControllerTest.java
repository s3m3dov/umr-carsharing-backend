package com.cimhub.notificationservice.controller;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.common.enums.UserType;
import com.cimhub.notificationservice.dto.request.CreateNotificationRequest;
import com.cimhub.notificationservice.dto.request.MarkNotificationReadRequest;
import com.cimhub.notificationservice.dto.response.NotificationResponse;
import com.cimhub.notificationservice.dto.response.NotificationStatsResponse;
import com.cimhub.notificationservice.exception.UnauthorizedNotificationAccessException;
import com.cimhub.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        notificationController = new NotificationController(notificationService);
    }

    @Test
    void createNotification_whenSenderMissingAndAuthenticated_shouldInjectAuthenticatedSender() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId("user-2")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .metadata(Map.of("k", "v"))
                .build();
        NotificationResponse created = sampleNotification("not-1", "user-2", "user-1");
        when(notificationService.createNotification(CreateNotificationRequest.builder()
                .recipientId("user-2")
                .senderId("user-1")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .relatedEntityId(null)
                .metadata(Map.of("k", "v"))
                .build())).thenReturn(created);

        ResponseEntity<?> response = notificationController.createNotification(
                "user-1",
                UserType.PASSENGER.name(),
                request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createNotification_whenSenderDiffersAndNotAdmin_shouldThrow() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId("user-2")
                .senderId("other-user")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .build();

        assertThrows(UnauthorizedNotificationAccessException.class,
                () -> notificationController.createNotification("user-1", UserType.PASSENGER.name(), request));
    }

    @Test
    void createNotification_whenSenderDiffersAndAdmin_shouldAllow() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId("user-2")
                .senderId("other-user")
                .title("Title")
                .message("Message")
                .type(NotificationType.BOOKING_REQUESTED)
                .build();
        when(notificationService.createNotification(request)).thenReturn(sampleNotification("n-1", "user-2", "other-user"));

        ResponseEntity<?> response = notificationController.createNotification("admin-1", UserType.ADMIN.name(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(notificationService).createNotification(request);
    }

    @Test
    void listNotifications_whenOwner_shouldReturnOk() {
        when(notificationService.listNotifications("user-1", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(sampleNotification("n-1", "user-1", "sender"))));

        ResponseEntity<?> response = notificationController.listNotifications(
                "user-1",
                "user-1",
                UserType.PASSENGER.name(),
                PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).listNotifications("user-1", PageRequest.of(0, 10));
    }

    @Test
    void listNotifications_whenMissingAuthHeader_shouldThrow() {
        assertThrows(UnauthorizedNotificationAccessException.class,
                () -> notificationController.listNotifications("user-1", null, UserType.PASSENGER.name(), PageRequest.of(0, 10)));
    }

    @Test
    void listNotifications_whenDifferentUserNonAdmin_shouldThrow() {
        assertThrows(UnauthorizedNotificationAccessException.class,
                () -> notificationController.listNotifications("user-2", "user-1", UserType.PASSENGER.name(), PageRequest.of(0, 10)));
    }

    @Test
    void listNotifications_whenAdmin_shouldAllow() {
        when(notificationService.listNotifications("user-2", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<?> response = notificationController.listNotifications(
                "user-2",
                "admin-user",
                UserType.ADMIN.name().toLowerCase(),
                PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).listNotifications("user-2", PageRequest.of(0, 10));
    }

    @Test
    void markAsRead_shouldValidateRecipientAccessAndDelegate() {
        MarkNotificationReadRequest request = MarkNotificationReadRequest.builder().recipientId("user-1").build();
        when(notificationService.markAsRead("n-1", "user-1")).thenReturn(sampleNotification("n-1", "user-1", "sender"));

        ResponseEntity<?> response = notificationController.markAsRead("n-1", "user-1", UserType.DRIVER.name(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).markAsRead("n-1", "user-1");
    }

    @Test
    void markAllAsRead_shouldReturnStats() {
        NotificationStatsResponse stats = NotificationStatsResponse.builder().recipientId("user-1").unreadCount(0).build();
        when(notificationService.markAllAsRead("user-1")).thenReturn(stats);

        ResponseEntity<?> response = notificationController.markAllAsRead("user-1", "user-1", UserType.DRIVER.name());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).markAllAsRead("user-1");
    }

    @Test
    void getStats_shouldReturnStats() {
        NotificationStatsResponse stats = NotificationStatsResponse.builder().recipientId("user-1").unreadCount(3).build();
        when(notificationService.getStats("user-1")).thenReturn(stats);

        ResponseEntity<?> response = notificationController.getStats("user-1", "user-1", UserType.PASSENGER.name());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).getStats("user-1");
    }

    @Test
    void getStats_whenInvalidRoleString_shouldTreatAsNonAdminAndDenyAccess() {
        assertThrows(UnauthorizedNotificationAccessException.class,
                () -> notificationController.getStats("user-2", "user-1", "not-a-role"));
    }

    private NotificationResponse sampleNotification(String id, String recipientId, String senderId) {
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
