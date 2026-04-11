package com.cimhub.authservice.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class UserServiceClientIntegrationTest {

    private MockRestServiceServer server;
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        userServiceClient = new UserServiceClient(builder.build());
        ReflectionTestUtils.setField(userServiceClient, "userServiceBaseUrl", "http://user-service.test");
    }

    // ── passenger profile ────────────────────────────────────────────────────

    @Test
    void createPassengerSubProfile_shouldSendExpectedRequestOnSuccess() {
        server.expect(requestTo("http://user-service.test/api/internal/passengers/passenger-1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "firstName": "Jane",
                          "lastName": "Passenger",
                          "phoneNumber": "+49 111",
                          "age": 31
                        }
                        """))
                .andRespond(withStatus(HttpStatus.CREATED));

        assertDoesNotThrow(
                () -> userServiceClient.createPassengerSubProfile("passenger-1", "Jane", "Passenger", "+49 111", 31));
        server.verify();
    }

    @Test
    void createDriverSubProfile_shouldSendExpectedRequestOnSuccess() {
        server.expect(requestTo("http://user-service.test/api/internal/drivers/driver-1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "firstName": "John",
                          "lastName": "Driver",
                          "phoneNumber": "+49 222",
                          "age": 44,
                          "licenseNumber": "LIC-001"
                        }
                        """))
                .andRespond(withStatus(HttpStatus.CREATED));

        assertDoesNotThrow(
                () -> userServiceClient.createDriverSubProfile("driver-1", "John", "Driver", "+49 222", 44, "LIC-001"));
        server.verify();
    }

    // ── conflict handling ────────────────────────────────────────────────────

    @Test
    void createPassengerSubProfile_whenConflict_shouldTreatAsIdempotentSuccess() {
        server.expect(requestTo("http://user-service.test/api/internal/passengers/passenger-1"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertDoesNotThrow(
                () -> userServiceClient.createPassengerSubProfile("passenger-1", "Jane", "Passenger", "+49 111", 31));
        server.verify();
    }

    // ── error propagation ────────────────────────────────────────────────────

    @Test
    void createPassengerSubProfile_whenClientError_shouldPropagateAsRuntimeException() {
        server.expect(requestTo("http://user-service.test/api/internal/passengers/passenger-2"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userServiceClient.createPassengerSubProfile("passenger-2", "Jane", "Passenger", "+49 111", 31));

        assertTrue(ex.getMessage().contains("Failed to create passenger sub-profile"));
        server.verify();
    }

    @Test
    void createDriverSubProfile_whenServerError_shouldPropagateAsRuntimeException() {
        server.expect(requestTo("http://user-service.test/api/internal/drivers/driver-2"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userServiceClient.createDriverSubProfile("driver-2", "John", "Driver", "+49 222", 44, "LIC-002"));

        assertTrue(ex.getMessage().contains("Failed to create driver sub-profile"));
        server.verify();
    }

    @Test
    void createDriverSubProfile_whenNetworkFailure_shouldPropagateAsRuntimeException() {
        server.expect(requestTo("http://user-service.test/api/internal/drivers/driver-3"))
                .andRespond(withException(new IOException("connection refused")));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userServiceClient.createDriverSubProfile("driver-3", "John", "Driver", "+49 222", 44, "LIC-003"));

        assertTrue(ex.getMessage().contains("Failed to create driver sub-profile"));
        assertTrue(ex.getCause() != null);
        server.verify();
    }
}
