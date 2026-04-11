package com.cimhub.reviewservice.service;

import com.cimhub.common.exception.DependencyUnavailableException;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.TripStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TripServiceClientIntegrationTest {

    private MockRestServiceServer server;
    private TripServiceClient tripServiceClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        tripServiceClient = new TripServiceClient(restClient);
        ReflectionTestUtils.setField(tripServiceClient, "tripServiceUrl", "http://trip-service.test");
    }

    @Test
    void isTripCompleted_shouldReturnTrueWhenTripStatusCompleted() throws Exception {
        String payload = wrappedJson(Map.ofEntries(
                Map.entry("tripStatus", TripStatus.COMPLETED.name()),
                Map.entry("driverId", "driver-1")));
        server.expect(requestTo("http://trip-service.test/api/trips/trip-1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertTrue(tripServiceClient.isTripCompleted("trip-1"));
        server.verify();
    }

    @Test
    void isTripCompleted_shouldReturnFalseWhenTripNotFound() {
        server.expect(requestTo("http://trip-service.test/api/trips/missing"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertFalse(tripServiceClient.isTripCompleted("missing"));
        server.verify();
    }

    @Test
    void isTripCompleted_shouldReturnFalseWhenTripNotCompleted() throws Exception {
        String payload = wrappedJson(Map.ofEntries(
                Map.entry("tripStatus", TripStatus.AVAILABLE.name()),
                Map.entry("driverId", "driver-1")));
        server.expect(requestTo("http://trip-service.test/api/trips/trip-available"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertFalse(tripServiceClient.isTripCompleted("trip-available"));
        server.verify();
    }

    @Test
    void isTripCompleted_shouldThrowDependencyUnavailableWhenServiceFails() {
        server.expect(requestTo("http://trip-service.test/api/trips/trip-2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.isTripCompleted("trip-2"));
        server.verify();
    }

    @Test
    void isTripCompleted_shouldThrowDependencyUnavailableWhenTripStatusMissing() throws Exception {
        String payload = wrappedJson(Map.of("driverId", "driver-1"));
        server.expect(requestTo("http://trip-service.test/api/trips/trip-3"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.isTripCompleted("trip-3"));
        server.verify();
    }

    @Test
    void isTripCompleted_shouldThrowDependencyUnavailableWhenPayloadMalformed() {
        server.expect(requestTo("http://trip-service.test/api/trips/trip-4"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.isTripCompleted("trip-4"));
        server.verify();
    }

    @Test
    void isBookingCompleted_shouldUseBookingStatusField() throws Exception {
        String payload = wrappedJson(Map.ofEntries(
                Map.entry("status", BookingStatus.COMPLETED.name()),
                Map.entry("passengerId", "passenger-1")));
        server.expect(requestTo("http://trip-service.test/api/bookings/booking-1"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertTrue(tripServiceClient.isBookingCompleted("booking-1"));
        server.verify();
    }

    @Test
    void getTripDriverId_shouldReturnDriverIdWhenPresent() throws Exception {
        String payload = wrappedJson(Map.ofEntries(
                Map.entry("tripStatus", TripStatus.COMPLETED.name()),
                Map.entry("driverId", "driver-7")));
        server.expect(requestTo("http://trip-service.test/api/trips/trip-7"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertEquals("driver-7", tripServiceClient.getTripDriverId("trip-7"));
        server.verify();
    }

    @Test
    void getTripDriverId_shouldReturnNullWhenTripNotFound() {
        server.expect(requestTo("http://trip-service.test/api/trips/trip-not-found"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertNull(tripServiceClient.getTripDriverId("trip-not-found"));
        server.verify();
    }

    @Test
    void getTripDriverId_shouldThrowDependencyUnavailableWhenMissingField() throws Exception {
        String payload = wrappedJson(Map.of("tripStatus", TripStatus.COMPLETED.name()));
        server.expect(requestTo("http://trip-service.test/api/trips/trip-no-driver"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.getTripDriverId("trip-no-driver"));
        server.verify();
    }

    @Test
    void getBookingPassengerId_shouldReturnPassengerIdWhenPresent() throws Exception {
        String payload = wrappedJson(Map.ofEntries(
                Map.entry("status", BookingStatus.COMPLETED.name()),
                Map.entry("passengerId", "passenger-7")));
        server.expect(requestTo("http://trip-service.test/api/bookings/booking-7"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertEquals("passenger-7", tripServiceClient.getBookingPassengerId("booking-7"));
        server.verify();
    }

    @Test
    void getBookingPassengerId_shouldThrowDependencyUnavailableWhenMissingField() throws Exception {
        String payload = wrappedJson(Map.of("status", BookingStatus.COMPLETED.name()));
        server.expect(requestTo("http://trip-service.test/api/bookings/booking-no-passenger"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.getBookingPassengerId("booking-no-passenger"));
        server.verify();
    }

    @Test
    void getBookingPassengerId_shouldThrowDependencyUnavailableOnServerError() {
        server.expect(requestTo("http://trip-service.test/api/bookings/booking-500"))
                .andRespond(withServerError());

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.getBookingPassengerId("booking-500"));
        server.verify();
    }

    private String wrappedJson(Map<String, String> fields) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dataNode = objectMapper.createObjectNode();
        fields.forEach(dataNode::put);
        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", dataNode);
        return objectMapper.writeValueAsString(root);
    }
}
