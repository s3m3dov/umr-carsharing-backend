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
              "driverId": "john.driver@example.com",
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
              "totalSeats": 3,
              "pricePerSeat": 5.0
            }""";

    public static final String CANCEL_TRIP_REQUEST = """
            {
              "tripId": "trip_001",
              "userId": "john.driver@example.com",
              "reason": "Change of plans"
            }""";

    // ---- Responses ----

    public static final String OFFER_TRIP_RESPONSE = """
            {
              "success": true,
              "data": {
                "tripId": "trip_001",
                "driverId": "john.driver@example.com",
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
                  "driverId": "john.driver@example.com",
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
              "status": 404,
              "code": "TRIP_NOT_FOUND",
              "message": "Trip trip_001 not found",
              "error": {
                "path": "/api/trips/trip_001",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12351"
            }""";

    public static final String ERROR_TRIP_IN_PROGRESS = """
            {
              "status": 409,
              "code": "TRIP_IN_PROGRESS",
              "message": "Cannot cancel trip that is already in progress",
              "error": {
                "path": "/api/trips/cancel",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12352"
            }""";

    public static final String ERROR_INVALID_TRIP_DATA = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid trip data provided",
              "error": {
                "path": "/api/trips/offer",
                "details": [
                  {
                    "field": "totalSeats",
                    "message": "Total seats must be between 1 and 8",
                    "type": "Min"
                  },
                  {
                    "field": "tripStartDateTime",
                    "message": "Trip start time must be in the future",
                    "type": "FutureOrPresent"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12357"
            }""";

    public static final String ERROR_INVALID_COORDINATES = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid coordinates or radius provided",
              "error": {
                "path": "/api/trips/search/near-source",
                "details": [
                  {
                    "field": "latitude",
                    "message": "Latitude must be between -90 and 90",
                    "type": "Range"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12358"
            }""";

    public static final String ERROR_UNAUTHORIZED = """
            {
              "status": 401,
              "code": "UNAUTHORIZED",
              "message": "Authentication required",
              "error": {
                "path": "/api/trips/offer",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12359"
            }""";
}
