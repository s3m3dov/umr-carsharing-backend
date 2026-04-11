package com.cimhub.reviewservice.service;

import com.cimhub.common.exception.DependencyUnavailableException;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.TripStatus;
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
        TripStatus tripStatus = readRequiredEnum(
                tripData,
                "tripStatus",
                TripStatus.class,
                "trip",
                tripId);
        return tripStatus == TripStatus.COMPLETED;
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
        BookingStatus bookingStatus = readRequiredEnum(
                bookingData,
                "status",
                BookingStatus.class,
                "booking",
                bookingId);
        return bookingStatus == BookingStatus.COMPLETED;
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
        return readRequiredText(tripData, "driverId", "trip", tripId);
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
        return readRequiredText(bookingData, "passengerId", "booking", bookingId);
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
                throw new DependencyUnavailableException(
                        "Trip service returned empty response while fetching " + resourceType + ": " + resourceId);
            }
            JsonNode data = response.get("data");
            if (data != null && data.isObject()) {
                return data;
            }
            throw new DependencyUnavailableException(
                    "Trip service returned malformed payload while fetching " + resourceType + ": " + resourceId);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException e) {
            log.error("Error calling trip-service for {}Id: {}", resourceType, resourceId, e);
            throw e;
        }
    }

    private String readRequiredText(JsonNode data, String field, String resourceType, String resourceId) {
        String value = data.path(field).asText(null);
        if (!StringUtils.hasText(value)) {
            throw new DependencyUnavailableException(
                    "Trip service returned malformed " + resourceType + " payload for id: " + resourceId);
        }
        return value;
    }

    private <T extends Enum<T>> T readRequiredEnum(JsonNode data,
                                                   String field,
                                                   Class<T> enumType,
                                                   String resourceType,
                                                   String resourceId) {
        String value = readRequiredText(data, field, resourceType, resourceId);
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException ex) {
            throw new DependencyUnavailableException(
                    "Trip service returned unsupported " + field + " value '" + value + "' for "
                            + resourceType + " id: " + resourceId);
        }
    }

}
