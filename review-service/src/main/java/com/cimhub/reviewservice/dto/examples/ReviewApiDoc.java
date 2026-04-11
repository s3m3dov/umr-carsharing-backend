package com.cimhub.reviewservice.dto.examples;

/**
 * OpenAPI example payloads for Review endpoints.
 * Referenced by {@link io.swagger.v3.oas.annotations.media.Schema} annotations.
 */
public final class ReviewApiDoc {

    private ReviewApiDoc() {}

    public static final String SUBMIT_REVIEW_REQUEST = """
            {
              "tripId": "trip_001",
              "bookingId": "booking_001",
              "rating": 5,
              "comment": "Great driver, very comfortable ride!"
            }""";

    public static final String SUBMIT_DRIVER_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": {
                "reviewId": "review_001",
                "tripId": "trip_001",
                "bookingId": "booking_001",
                "reviewerId": "jane.passenger@example.com",
                "reviewerType": "PASSENGER",
                "revieweeId": "john.driver@example.com",
                "revieweeType": "DRIVER",
                "rating": 5,
                "comment": "Great driver, very comfortable ride!",
                "status": "PUBLISHED",
                "createdAt": "2026-03-25T10:00:00.000Z",
                "updatedAt": "2026-03-25T10:00:00.000Z"
              },
              "message": "Review submitted successfully"
            }""";

    public static final String SUBMIT_PASSENGER_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": {
                "reviewId": "review_002",
                "tripId": "trip_001",
                "bookingId": "booking_001",
                "reviewerId": "john.driver@example.com",
                "reviewerType": "DRIVER",
                "revieweeId": "jane.passenger@example.com",
                "revieweeType": "PASSENGER",
                "rating": 4,
                "comment": "Good passenger, on time and polite",
                "status": "PUBLISHED",
                "createdAt": "2026-03-25T10:30:00.000Z",
                "updatedAt": "2026-03-25T10:30:00.000Z"
              },
              "message": "Review submitted successfully"
            }""";

    public static final String GET_DRIVER_REVIEWS_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "reviewId": "review_001",
                  "tripId": "trip_001",
                  "bookingId": "booking_001",
                  "reviewerId": "jane.passenger@example.com",
                  "reviewerType": "PASSENGER",
                  "revieweeId": "john.driver@example.com",
                  "revieweeType": "DRIVER",
                  "rating": 5,
                  "comment": "Great driver!",
                  "status": "PUBLISHED",
                  "createdAt": "2026-03-25T10:00:00.000Z",
                  "updatedAt": "2026-03-25T10:00:00.000Z"
                }
              ],
              "message": "Reviews retrieved successfully"
            }""";

    public static final String GET_PASSENGER_REVIEWS_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "reviewId": "review_002",
                  "tripId": "trip_001",
                  "bookingId": "booking_001",
                  "reviewerId": "john.driver@example.com",
                  "reviewerType": "DRIVER",
                  "revieweeId": "jane.passenger@example.com",
                  "revieweeType": "PASSENGER",
                  "rating": 4,
                  "comment": "Good passenger!",
                  "status": "PUBLISHED",
                  "createdAt": "2026-03-25T10:30:00.000Z",
                  "updatedAt": "2026-03-25T10:30:00.000Z"
                }
              ],
              "message": "Reviews retrieved successfully"
            }""";

    public static final String GET_AVERAGE_RATING_RESPONSE = """
            {
              "success": true,
              "data": {
                "subjectId": "john.driver@example.com",
                "averageRating": 4.75,
                "totalReviews": 4
              },
              "message": "Average rating retrieved successfully"
            }""";

    public static final String ERROR_REVIEW_ALREADY_EXISTS = """
            {
              "status": 409,
              "code": "REVIEW_ALREADY_EXISTS",
              "message": "A review for this trip already exists",
              "error": {
                "path": "/api/driver-reviews",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12351"
            }""";

    public static final String ERROR_TRIP_NOT_COMPLETED = """
            {
              "status": 400,
              "code": "TRIP_NOT_COMPLETED",
              "message": "Cannot review a trip that has not been completed",
              "error": {
                "path": "/api/driver-reviews",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12352"
            }""";

    public static final String ERROR_UNAUTHORIZED_REVIEWER = """
            {
              "status": 403,
              "code": "UNAUTHORIZED_REVIEWER",
              "message": "Only participants of the trip can submit a review",
              "error": {
                "path": "/api/driver-reviews",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12353"
            }""";

    public static final String ERROR_DRIVER_NOT_FOUND = """
            {
              "status": 404,
              "code": "DRIVER_NOT_FOUND",
              "message": "Driver not found",
              "error": {
                "path": "/api/driver-reviews/john.driver@example.com",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12354"
            }""";

    public static final String ERROR_PASSENGER_NOT_FOUND = """
            {
              "status": 404,
              "code": "PASSENGER_NOT_FOUND",
              "message": "Passenger not found",
              "error": {
                "path": "/api/passenger-reviews/jane.passenger@example.com",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12355"
            }""";

    public static final String ERROR_INVALID_RATING = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid review data provided",
              "error": {
                "path": "/api/driver-reviews",
                "details": [
                  {
                    "field": "rating",
                    "message": "Rating must be between 1 and 5",
                    "type": "Range"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12357"
            }""";

    // ---- Admin Responses ----

    public static final String ADMIN_LIST_REVIEWS_RESPONSE = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "reviewId": "review_001",
                    "tripId": "trip_001",
                    "bookingId": "booking_001",
                    "reviewerId": "jane.passenger@example.com",
                    "reviewerType": "PASSENGER",
                    "revieweeId": "john.driver@example.com",
                    "revieweeType": "DRIVER",
                    "rating": 5,
                    "comment": "Great driver!",
                    "status": "PUBLISHED",
                    "createdAt": "2026-03-25T10:00:00.000Z",
                    "updatedAt": "2026-03-25T10:00:00.000Z"
                  }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
              },
              "message": "Reviews retrieved successfully"
            }""";

    public static final String ADMIN_GET_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": {
                "reviewId": "review_001",
                "tripId": "trip_001",
                "bookingId": "booking_001",
                "reviewerId": "jane.passenger@example.com",
                "reviewerType": "PASSENGER",
                "revieweeId": "john.driver@example.com",
                "revieweeType": "DRIVER",
                "rating": 5,
                "comment": "Great driver!",
                "status": "PUBLISHED",
                "createdAt": "2026-03-25T10:00:00.000Z",
                "updatedAt": "2026-03-25T10:00:00.000Z"
              },
              "message": "Review retrieved successfully"
            }""";

    public static final String ADMIN_FLAG_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": {
                "reviewId": "review_001",
                "tripId": "trip_001",
                "bookingId": "booking_001",
                "reviewerId": "jane.passenger@example.com",
                "reviewerType": "PASSENGER",
                "revieweeId": "john.driver@example.com",
                "revieweeType": "DRIVER",
                "rating": 5,
                "comment": "Great driver!",
                "status": "FLAGGED",
                "createdAt": "2026-03-25T10:00:00.000Z",
                "updatedAt": "2026-03-25T14:00:00.000Z"
              },
              "message": "Review flagged successfully"
            }""";

    public static final String ADMIN_REMOVE_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": "Review removed successfully",
              "message": "Review removed"
            }""";

    public static final String ADMIN_PUBLISH_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": {
                "reviewId": "review_001",
                "tripId": "trip_001",
                "bookingId": "booking_001",
                "reviewerId": "jane.passenger@example.com",
                "reviewerType": "PASSENGER",
                "revieweeId": "john.driver@example.com",
                "revieweeType": "DRIVER",
                "rating": 5,
                "comment": "Great driver!",
                "status": "PUBLISHED",
                "createdAt": "2026-03-25T10:00:00.000Z",
                "updatedAt": "2026-03-25T15:00:00.000Z"
              },
              "message": "Review published successfully"
            }""";

    public static final String ERROR_REVIEW_NOT_FOUND = """
            {
              "status": 404,
              "code": "REVIEW_NOT_FOUND",
              "message": "Review review_001 not found",
              "error": {
                "path": "/api/admin/reviews/review_001",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12380"
            }""";

    public static final String ERROR_ADMIN_UNAUTHORIZED = """
            {
              "status": 403,
              "code": "UNAUTHORIZED",
              "message": "Authentication required",
              "error": {
                "path": "/api/admin/reviews",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12381"
            }""";
}
