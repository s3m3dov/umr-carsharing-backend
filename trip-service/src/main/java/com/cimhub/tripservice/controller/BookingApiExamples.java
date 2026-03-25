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
              "success": false,
              "error": "TRIP_FULL",
              "message": "No available seats on this trip"
            }""";
}
