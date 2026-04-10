package com.cimhub.reviewservice.service;

import com.cimhub.common.exception.DependencyUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

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

class TripServiceClientTest {

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
        String payload = wrappedJson("tripStatus", "COMPLETED", "driverId", "driver-1");
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
    void isTripCompleted_shouldThrowDependencyUnavailableWhenServiceFails() {
        server.expect(requestTo("http://trip-service.test/api/trips/trip-2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(DependencyUnavailableException.class,
                () -> tripServiceClient.isTripCompleted("trip-2"));
        server.verify();
    }

    @Test
    void isBookingCompleted_shouldUseBookingStatusField() throws Exception {
        String payload = wrappedJson("status", "COMPLETED", "passengerId", "passenger-1");
        server.expect(requestTo("http://trip-service.test/api/bookings/booking-1"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertTrue(tripServiceClient.isBookingCompleted("booking-1"));
        server.verify();
    }

    @Test
    void getTripDriverId_shouldReturnNullWhenMissingFieldOrNotFound() throws Exception {
        String noDriverPayload = wrappedJson("tripStatus", "COMPLETED");
        server.expect(requestTo("http://trip-service.test/api/trips/trip-no-driver"))
                .andRespond(withSuccess(noDriverPayload, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://trip-service.test/api/trips/trip-not-found"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertNull(tripServiceClient.getTripDriverId("trip-no-driver"));
        assertNull(tripServiceClient.getTripDriverId("trip-not-found"));
        server.verify();
    }

    @Test
    void getBookingPassengerId_shouldReturnPassengerIdWhenPresent() throws Exception {
        String payload = wrappedJson("status", "COMPLETED", "passengerId", "passenger-7");
        server.expect(requestTo("http://trip-service.test/api/bookings/booking-7"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertEquals("passenger-7", tripServiceClient.getBookingPassengerId("booking-7"));
        server.verify();
    }

    private String wrappedJson(Object... keyValues) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        var dataNode = objectMapper.createObjectNode();
        for (int i = 0; i < keyValues.length; i += 2) {
            dataNode.put(String.valueOf(keyValues[i]), String.valueOf(keyValues[i + 1]));
        }
        var root = objectMapper.createObjectNode();
        root.set("data", dataNode);
        return objectMapper.writeValueAsString(root);
    }
}
