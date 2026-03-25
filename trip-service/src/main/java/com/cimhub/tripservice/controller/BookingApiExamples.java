package com.cimhub.tripservice.controller;

/**
 * OpenAPI example payloads for Booking endpoints.
 * Referenced by {@link BookingApi} annotations.
 */
public final class BookingApiExamples {

    private BookingApiExamples() {}

    // ---- Requests ----

    public static final String JOIN_TRIP_REQUEST = """
            {
              "tripId": "trip_001",
              "passengerId": "user_456",
              "driverId": "user_123",
              "pickupPoint": {
                "latitude": 50.8154,
                "longitude": 8.6747
              },
              "destinationPoint": {
                "latitude": 50.7265,
                "longitude": 7.0895
              },
              "rideStartTime": "2024-03-25T08:00:00Z",
              "requestedSeats": 1
            }""";

    public static final String CANCEL_BOOKING_REQUEST = """
            {
              "tripId": "trip_001",
              "userId": "user_456",
              "rideId": "ride_001",
              "cancellationReason": "Change of plans"
            }""";

    // ---- Responses ----

    public static final String JOIN_TRIP_RESPONSE = """
            {
              "success": true,
              "data": "Booking confirmed for trip trip_001",
              "message": "Operation successful"
            }""";

    public static final String CANCEL_BOOKING_RESPONSE = """
            {
              "success": true,
              "data": "Booking cancelled successfully",
              "message": "Operation successful"
            }""";

    public static final String ACTIVE_BOOKINGS_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "rideId": "ride_001",
                  "tripId": "trip_001",
                  "driverId": "user_123",
                  "vehicleNumber": "HE-XX-1234",
                  "rideStatus": "CONFIRMED",
                  "tripStartDateTime": "2024-03-25T08:00:00Z",
                  "bookedSeats": 1,
                  "estimatedFare": 12.50
                }
              ],
              "message": "Operation successful"
            }""";

    // ---- Errors ----

    public static final String ERROR_TRIP_FULL = """
            {
              "status": 409,
              "code": "INSUFFICIENT_SEATS",
              "message": "No available seats on this trip",
              "error": {
                "path": "/api/bookings/join",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12353"
            }""";

    public static final String ERROR_TRIP_NOT_FOUND = """
            {
              "status": 404,
              "code": "TRIP_NOT_FOUND",
              "message": "Trip trip_001 not found",
              "error": {
                "path": "/api/bookings/join",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12360"
            }""";

    public static final String ERROR_BOOKING_NOT_FOUND = """
            {
              "status": 404,
              "code": "BOOKING_NOT_FOUND",
              "message": "Booking not found",
              "error": {
                "path": "/api/bookings/cancel",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12361"
            }""";

    public static final String ERROR_INVALID_BOOKING_DATA = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid booking data provided",
              "error": {
                "path": "/api/bookings/join",
                "details": [
                  {
                    "field": "requestedSeats",
                    "message": "Requested seats must be at least 1",
                    "type": "Min"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12362"
            }""";

    public static final String ERROR_TRIP_IN_PROGRESS = """
            {
              "status": 409,
              "code": "TRIP_IN_PROGRESS",
              "message": "Cannot cancel booking for trip that is already in progress",
              "error": {
                "path": "/api/bookings/cancel",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12363"
            }""";
}
