package com.cimhub.tripservice.controller;

/**
 * OpenAPI example payloads for Trip endpoints.
 * Referenced by {@link TripApi} annotations.
 */
public final class TripApiExamples {

    private TripApiExamples() {}

    // ---- Requests ----

    public static final String OFFER_TRIP_REQUEST = """
            {
              "driverId": "user_123",
              "vehicleNumber": "HE-XX-1234",
              "sourceAddress": {
                "latitude": 50.8154,
                "longitude": 8.6747
              },
              "destinationAddress": {
                "latitude": 50.7265,
                "longitude": 7.0895
              },
              "tripStartDateTime": "2024-03-25T08:00:00Z",
              "totalSeats": 3
            }""";

    public static final String CANCEL_TRIP_REQUEST = """
            {
              "tripId": "trip_001",
              "userId": "user_123",
              "reason": "Change of plans"
            }""";

    // ---- Responses ----

    public static final String OFFER_TRIP_RESPONSE = """
            {
              "success": true,
              "data": {
                "tripId": "trip_001",
                "driverId": "user_123",
                "departureTime": "2024-03-25T08:00:00Z",
                "availableSeats": 3,
                "status": "SCHEDULED"
              },
              "message": "Trip created successfully"
            }""";

    public static final String SEARCH_TRIPS_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "tripId": "trip_001",
                  "driverId": "user_123",
                  "departureTime": "2024-03-25T08:00:00Z",
                  "sourceLocation": {
                    "latitude": 50.8154,
                    "longitude": 8.6747
                  },
                  "destinationLocation": {
                    "latitude": 50.7265,
                    "longitude": 7.0895
                  },
                  "availableSeats": 3,
                  "pricePerSeat": 15.50
                }
              ],
              "message": "Trips found"
            }""";

    public static final String ACTIVE_TRIPS_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "tripId": "trip_001",
                  "status": "SCHEDULED",
                  "departureTime": "2024-03-25T08:00:00Z",
                  "destinationLocation": {
                    "latitude": 50.7265,
                    "longitude": 7.0895
                  },
                  "currentPassengers": 2,
                  "availableSeats": 1
                }
              ],
              "message": "Active trips retrieved"
            }""";

    public static final String CANCEL_TRIP_RESPONSE = """
            {
              "success": true,
              "data": "Trip cancelled successfully",
              "message": "Trip cancelled"
            }""";

    // ---- Errors ----

    public static final String ERROR_TRIP_NOT_FOUND = """
            {
              "success": false,
              "error": "TRIP_NOT_FOUND",
              "message": "Trip trip_001 not found"
            }""";

    public static final String ERROR_TRIP_IN_PROGRESS = """
            {
              "success": false,
              "error": "TRIP_IN_PROGRESS",
              "message": "Cannot cancel trip that is already in progress"
            }""";
}
