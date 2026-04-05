package com.cimhub.tripservice.service;

import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DriverNotificationServiceTest {

    @Test
    void notifyMethods_shouldPostNotificationsToConfiguredEndpoint() {
        List<String> urls = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            urls.add(request.url().toString());
            return reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.OK).build());
        });

        DriverNotificationService service = new DriverNotificationService(builder);
        ReflectionTestUtils.setField(service, "notificationServiceBaseUrl", "http://notify.example");

        Trip withStartTime = new Trip();
        withStartTime.setTripStartDateTimeUTC(Instant.parse("2026-06-01T10:00:00Z"));
        Booking booking = booking();

        assertDoesNotThrow(() -> service.notifyBookingRequested(withStartTime, booking));
        assertDoesNotThrow(() -> service.notifyBookingConfirmed(withStartTime, booking));

        Trip withoutStartTime = new Trip();
        assertDoesNotThrow(() -> service.notifyBookingCancelledByPassenger(withoutStartTime, booking));

        assertEquals(3, urls.size());
        assertEquals("http://notify.example/api/notifications", urls.get(0));
        assertEquals("http://notify.example/api/notifications", urls.get(1));
        assertEquals("http://notify.example/api/notifications", urls.get(2));
    }

    @Test
    void notifyMethods_whenNotificationCallFails_shouldSwallowException() {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                reactor.core.publisher.Mono.error(new RuntimeException("notification service unavailable")));

        DriverNotificationService service = new DriverNotificationService(builder);
        ReflectionTestUtils.setField(service, "notificationServiceBaseUrl", "http://notify.example");

        Trip trip = new Trip();
        trip.setTripStartDateTimeUTC(Instant.parse("2026-06-01T10:00:00Z"));

        assertDoesNotThrow(() -> service.notifyBookingRequested(trip, booking()));
    }

    private Booking booking() {
        Booking booking = new Booking();
        booking.setBookingId("b-1");
        booking.setTripId("trip-1");
        booking.setDriverId("driver-1");
        booking.setPassengerId("passenger-1");
        booking.setRequestedSeats(2);
        return booking;
    }
}
