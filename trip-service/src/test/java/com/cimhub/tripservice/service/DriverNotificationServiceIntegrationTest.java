package com.cimhub.tripservice.service;

import com.cimhub.common.enums.NotificationType;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.notification.DriverNotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverNotificationServiceIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── request contract ─────────────────────────────────────────────────────

    @Test
    void notifyBookingRequested_shouldSendExpectedRequestPayload() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        DriverNotificationService service = serviceWithCapture(captured);

        Trip trip = new Trip();
        trip.setTripStartDateTimeUTC(Instant.parse("2026-06-01T10:00:00Z"));
        Booking booking = booking();

        service.notifyBookingRequested(trip, booking);

        CapturedRequest request = captured.get();
        assertNotNull(request);
        assertEquals("http://notify.example/api/notifications", request.url());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, request.contentType());

        JsonNode payload = objectMapper.readTree(request.body());
        assertEquals("driver-1", payload.path("recipientId").asText());
        assertEquals("passenger-1", payload.path("senderId").asText());
        assertEquals("New booking request", payload.path("title").asText());
        assertEquals(NotificationType.BOOKING_REQUESTED.name(), payload.path("type").asText());
        assertEquals("b-1", payload.path("relatedEntityId").asText());
        assertEquals("trip-1", payload.path("metadata").path("tripId").asText());
        assertEquals("b-1", payload.path("metadata").path("bookingId").asText());
        assertEquals("2026-06-01T10:00:00Z", payload.path("metadata").path("tripStartDateTimeUTC").asText());
    }

    @Test
    void notifyBookingCancelledByPassenger_shouldOmitTripStartTimeWhenUnavailable() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        DriverNotificationService service = serviceWithCapture(captured);

        service.notifyBookingCancelledByPassenger(new Trip(), booking());

        JsonNode payload = objectMapper.readTree(captured.get().body());
        assertEquals(NotificationType.BOOKING_CANCELLED_BY_PASSENGER.name(), payload.path("type").asText());
        assertFalse(payload.path("metadata").has("tripStartDateTimeUTC"));
    }

    @Test
    void notifyRideCompleted_shouldSendToPassengerWithExpectedType() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        DriverNotificationService service = serviceWithCapture(captured);

        service.notifyRideCompleted(new Trip(), booking());

        JsonNode payload = objectMapper.readTree(captured.get().body());
        assertEquals("passenger-1", payload.path("recipientId").asText());
        assertEquals("driver-1", payload.path("senderId").asText());
        assertEquals(NotificationType.RIDE_COMPLETED.name(), payload.path("type").asText());
        assertTrue(payload.path("message").asText().contains("trip-1"));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private DriverNotificationService serviceWithCapture(AtomicReference<CapturedRequest> captured) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            MockClientHttpRequest mockRequest = new MockClientHttpRequest(request.method(), request.url());
            request.body().insert(mockRequest, new BodyInserter.Context() {
                @Override
                public List<HttpMessageWriter<?>> messageWriters() {
                    return ExchangeStrategies.withDefaults().messageWriters();
                }

                @Override
                public Optional<ServerHttpRequest> serverRequest() {
                    return Optional.empty();
                }

                @Override
                public Map<String, Object> hints() {
                    return Collections.emptyMap();
                }
            }).block();

            captured.set(new CapturedRequest(
                    request.url().toString(),
                    request.headers().getFirst(HttpHeaders.CONTENT_TYPE),
                    mockRequest.getBodyAsString().block()));

            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        });

        DriverNotificationService service = new DriverNotificationService(builder);
        ReflectionTestUtils.setField(service, "notificationServiceBaseUrl", "http://notify.example");
        return service;
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

    private record CapturedRequest(String url, String contentType, String body) {
    }
}
