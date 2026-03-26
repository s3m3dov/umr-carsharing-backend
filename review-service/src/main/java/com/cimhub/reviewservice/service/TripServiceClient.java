package com.cimhub.reviewservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Client for calling the trip-service to validate trip and booking completion status.
 * Note: Returns false silently on errors - this may mask service unavailability issues.
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
     * @return true if trip is completed, false otherwise (including errors)
     */
    public boolean isTripCompleted(String tripId) {
        try {
            String url = tripServiceUrl + "/api/trips/" + tripId;
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tripData = (Map<String, Object>) data;
                    Object status = tripData.get("tripStatus");
                    return "COMPLETED".equals(status);
                }
            }
            return false;
        } catch (RestClientException e) {
            log.error("Error calling trip-service for tripId: {}", tripId, e);
            return false;
        }
    }

    /**
     * Fetch booking details from trip-service to verify it's completed.
     *
     * @param bookingId the booking ID
     * @return true if booking is completed, false otherwise (including errors)
     */
    public boolean isBookingCompleted(String bookingId) {
        try {
            String url = tripServiceUrl + "/api/bookings/" + bookingId;
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bookingData = (Map<String, Object>) data;
                    Object status = bookingData.get("status");
                    return "COMPLETED".equals(status);
                }
            }
            return false;
        } catch (RestClientException e) {
            log.error("Error calling trip-service for bookingId: {}", bookingId, e);
            return false;
        }
    }

    /**
     * Get the driver ID for a trip.
     *
     * @param tripId the trip ID
     * @return driver ID if found, null otherwise
     */
    public String getTripDriverId(String tripId) {
        try {
            String url = tripServiceUrl + "/api/trips/" + tripId;
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tripData = (Map<String, Object>) data;
                    Object driverId = tripData.get("driverId");
                    return driverId != null ? driverId.toString() : null;
                }
            }
            return null;
        } catch (RestClientException e) {
            log.error("Error calling trip-service for tripId: {}", tripId, e);
            return null;
        }
    }

    /**
     * Get the passenger ID for a booking.
     *
     * @param bookingId the booking ID
     * @return passenger ID if found, null otherwise
     */
    public String getBookingPassengerId(String bookingId) {
        try {
            String url = tripServiceUrl + "/api/bookings/" + bookingId;
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bookingData = (Map<String, Object>) data;
                    Object passengerId = bookingData.get("passengerId");
                    return passengerId != null ? passengerId.toString() : null;
                }
            }
            return null;
        } catch (RestClientException e) {
            log.error("Error calling trip-service for bookingId: {}", bookingId, e);
            return null;
        }
    }
}
