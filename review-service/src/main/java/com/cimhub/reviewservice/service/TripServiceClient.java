package com.cimhub.reviewservice.service;

import com.cimhub.common.exception.DependencyUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client for calling the trip-service to validate trip and booking completion status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceClient {

    private final RestClient restClient;

    @Value("${trip-service.url:http://trip-service:8080}")
    private String tripServiceUrl;

    /**
     * Fetch trip details from trip-service to verify it's completed.
     *
     * @param tripId the trip ID
     * @return true if trip is completed, false otherwise
     */
    public boolean isTripCompleted(String tripId) {
        JsonNode tripData = fetchTripData(tripId);
        if (tripData == null) {
            return false;
        }
        return "COMPLETED".equals(tripData.path("tripStatus").asText(null));
    }

    /**
     * Fetch booking details from trip-service to verify it's completed.
     *
     * @param bookingId the booking ID
     * @return true if booking is completed, false otherwise
     */
    public boolean isBookingCompleted(String bookingId) {
        JsonNode bookingData = fetchBookingData(bookingId);
        if (bookingData == null) {
            return false;
        }
        return "COMPLETED".equals(bookingData.path("status").asText(null));
    }

    /**
     * Get the driver ID for a trip.
     *
     * @param tripId the trip ID
     * @return driver ID if found, null otherwise
     */
    public String getTripDriverId(String tripId) {
        JsonNode tripData = fetchTripData(tripId);
        if (tripData == null) {
            return null;
        }
        String driverId = tripData.path("driverId").asText(null);
        return StringUtils.hasText(driverId) ? driverId : null;
    }

    /**
     * Get the passenger ID for a booking.
     *
     * @param bookingId the booking ID
     * @return passenger ID if found, null otherwise
     */
    public String getBookingPassengerId(String bookingId) {
        JsonNode bookingData = fetchBookingData(bookingId);
        if (bookingData == null) {
            return null;
        }
        String passengerId = bookingData.path("passengerId").asText(null);
        return StringUtils.hasText(passengerId) ? passengerId : null;
    }

    private JsonNode fetchTripData(String tripId) {
        return fetchResourceData("/api/trips/" + tripId, "trip", tripId);
    }

    private JsonNode fetchBookingData(String bookingId) {
        return fetchResourceData("/api/bookings/" + bookingId, "booking", bookingId);
    }

    private JsonNode fetchResourceData(String endpoint, String resourceType, String resourceId) {
        try {
            return executeRequest(endpoint, resourceType, resourceId);
        } catch (RestClientException ex) {
            throw new DependencyUnavailableException(
                    "Trip service is unavailable while fetching " + resourceType + ": " + resourceId);
        }
    }

    private JsonNode executeRequest(String endpoint, String resourceType, String resourceId) {
        try {
            String url = tripServiceUrl + endpoint;
            JsonNode response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                return null;
            }
            JsonNode data = response.get("data");
            return data != null && data.isObject() ? data : null;
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException e) {
            log.error("Error calling trip-service for {}Id: {}", resourceType, resourceId, e);
            throw e;
        }
    }
}
