package com.cimhub.tripservice.dto.examples;

/**
 * OpenAPI example payloads for Booking endpoints.
 * Referenced by {@link io.swagger.v3.oas.annotations.media.Schema} annotations.
 */
public final class BookingApiDoc {

    private BookingApiDoc() {}

    // ---- Requests ----

    public static final String JOIN_TRIP_REQUEST = """
            {
              "tripId": "trip_001",
              "driverId": "john.driver@example.com",
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
                  "driverId": "john.driver@example.com",
                  "vehicleNumber": "HE-XX-1234",
                  "rideStatus": "CONFIRMED",
                  "tripStartDateTime": "2024-03-25T08:00:00Z",
                  "bookedSeats": 1
                  }              ],
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

    // ---- Admin Responses ----

    public static final String ADMIN_LIST_BOOKINGS_RESPONSE = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "rideId": "ride_001",
                    "tripId": "trip_001",
                    "passengerId": "jane.passenger@example.com",
                    "driverId": "john.driver@example.com",
                    "pickupLocation": {
                      "latitude": 50.8154,
                      "longitude": 8.6747
                    },
                    "dropoffLocation": {
                      "latitude": 50.7265,
                      "longitude": 7.0895
                    },
                    "requestedSeats": 2,
                    "fare": 31.00,
                    "bookingStatus": "CONFIRMED",
                    "createdAt": "2026-03-30T12:00:00.000Z"
                  }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
              },
              "message": "Bookings retrieved successfully"
            }""";

    public static final String ADMIN_GET_BOOKING_RESPONSE = """
            {
              "success": true,
              "data": {
                "rideId": "ride_001",
                "tripId": "trip_001",
                "passengerId": "jane.passenger@example.com",
                "driverId": "john.driver@example.com",
                "pickupLocation": {
                  "latitude": 50.8154,
                  "longitude": 8.6747
                },
                "dropoffLocation": {
                  "latitude": 50.7265,
                  "longitude": 7.0895
                },
                "requestedSeats": 2,
                "fare": 31.00,
                "bookingStatus": "CONFIRMED",
                "createdAt": "2026-03-30T12:00:00.000Z"
              },
              "message": "Booking retrieved successfully"
            }""";

    public static final String ADMIN_UPDATE_BOOKING_RESPONSE = """
            {
              "success": true,
              "data": {
                "rideId": "ride_001",
                "tripId": "trip_001",
                "passengerId": "jane.passenger@example.com",
                "driverId": "john.driver@example.com",
                "pickupLocation": {
                  "latitude": 50.8154,
                  "longitude": 8.6747
                },
                "dropoffLocation": {
                  "latitude": 50.7265,
                  "longitude": 7.0895
                },
                "requestedSeats": 2,
                "fare": 31.00,
                "bookingStatus": "CONFIRMED",
                "createdAt": "2026-03-30T12:00:00.000Z"
              },
              "message": "Booking updated successfully"
            }""";

    public static final String ADMIN_CANCEL_BOOKING_RESPONSE = """
            {
              "success": true,
              "data": "Booking cancelled successfully",
              "message": "Booking cancelled"
            }""";

    // ---- Errors ----

    public static final String ERROR_UNAUTHORIZED = """
            {
              "status": 403,
              "code": "UNAUTHORIZED",
              "message": "Authentication required",
              "error": {
                "path": "/api/admin/bookings",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12380"
            }""";
}
