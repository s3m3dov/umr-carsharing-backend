package com.cimhub.tripservice.service;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverNotificationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.notification.base-url:http://notification-service:8080}")
    private String notificationServiceBaseUrl;

    public void notifyBookingRequested(Trip trip, Booking booking) {
        String message = String.format(
                "Passenger %s requested %d seat(s) on your trip %s.",
                booking.getPassengerId(),
                booking.getRequestedSeats(),
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getDriverId(),
                booking.getPassengerId(),
                "New booking request",
                message,
                NotificationType.BOOKING_REQUESTED,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyBookingConfirmed(Trip trip, Booking booking) {
        String message = String.format(
                "Passenger %s confirmed %d seat(s) on your trip %s.",
                booking.getPassengerId(),
                booking.getRequestedSeats(),
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getDriverId(),
                booking.getPassengerId(),
                "New booking confirmed",
                message,
                NotificationType.BOOKING_CONFIRMED,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyRideCompleted(Trip trip, Booking booking) {
        String message = String.format(
                "Your ride on trip %s has been completed. Thank you for riding!",
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getPassengerId(),
                booking.getDriverId(),
                "Ride completed",
                message,
                NotificationType.RIDE_COMPLETED,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyTripCompleted(Trip trip, Booking booking) {
        String message = String.format(
                "Your ride on trip %s has been completed by the driver. Thank you for riding!",
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getPassengerId(),
                booking.getDriverId(),
                "Trip completed",
                message,
                NotificationType.TRIP_COMPLETED,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyBookingCancelledByPassenger(Trip trip, Booking booking) {
        String message = String.format(
                "Passenger %s cancelled booking %s for trip %s.",
                booking.getPassengerId(),
                booking.getBookingId(),
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getDriverId(),
                booking.getPassengerId(),
                "Booking cancelled",
                message,
                NotificationType.BOOKING_CANCELLED_BY_PASSENGER,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyPassengerBookingAccepted(Trip trip, Booking booking) {
        String message = String.format(
                "Your booking for trip %s has been accepted by the driver.",
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getPassengerId(),
                booking.getDriverId(),
                "Booking accepted",
                message,
                NotificationType.BOOKING_CONFIRMED,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyPassengerBookingRejected(Trip trip, Booking booking) {
        String message = String.format(
                "Your booking for trip %s has been rejected by the driver.",
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getPassengerId(),
                booking.getDriverId(),
                "Booking rejected",
                message,
                NotificationType.BOOKING_REJECTED,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    public void notifyPassengerTripCancelledByDriver(Trip trip, Booking booking) {
        String message = String.format(
                "The driver has cancelled trip %s. Your booking has been automatically cancelled.",
                booking.getTripId());

        sendNotification(new CreateNotificationRequest(
                booking.getPassengerId(),
                booking.getDriverId(),
                "Trip cancelled",
                message,
                NotificationType.BOOKING_CANCELLED_BY_DRIVER,
                booking.getBookingId(),
                metadata(trip, booking)));
    }

    private void sendNotification(CreateNotificationRequest request) {
        try {
            webClientBuilder
                    .baseUrl(notificationServiceBaseUrl)
                    .build()
                    .post()
                    .uri("/api/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(3))
                    .block();
        } catch (Exception ex) {
            log.warn("Failed to send driver notification to {} via {}: {}",
                    request.recipientId(),
                    notificationServiceBaseUrl,
                    ex.getMessage());
        }
    }

    private Map<String, Object> metadata(Trip trip, Booking booking) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tripId", booking.getTripId());
        metadata.put("bookingId", booking.getBookingId());
        metadata.put("driverId", booking.getDriverId());
        metadata.put("passengerId", booking.getPassengerId());
        metadata.put("requestedSeats", booking.getRequestedSeats());

        if (trip.getTripStartDateTimeUTC() != null) {
            metadata.put("tripStartDateTimeUTC", trip.getTripStartDateTimeUTC().toString());
        }

        return metadata;
    }

    private record CreateNotificationRequest(
            String recipientId,
            String senderId,
            String title,
            String message,
            NotificationType type,
            String relatedEntityId,
            Map<String, Object> metadata) {
    }
}
