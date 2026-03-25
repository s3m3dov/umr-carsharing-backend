package com.cimhub.reviewservice.controller;

/**
 * OpenAPI example payloads for Review endpoints.
 * Referenced by {@link Schema} annotations.
 */
public final class ReviewApiExamples {

    private ReviewApiExamples() {}

    // ---- Requests ----

    public static final String SUBMIT_REVIEW_REQUEST = """
            {
              "tripId": "trip_001",
              "bookingId": "booking_001",
              "rating": 5,
              "comment": "Great driver, very comfortable ride!"
            }""";

    // ---- Responses ----

    public static final String SUBMIT_REVIEW_RESPONSE = """
            {
              "success": true,
              "data": {
                "reviewId": "review_001",
                "tripId": "trip_001",
                "reviewerId": "user_123",
                "reviewerRole": "PASSENGER",
                "subjectId": "user_456",
                "subjectRole": "DRIVER",
                "rating": 5,
                "comment": "Great driver, very comfortable ride!",
                "status": "PUBLISHED",
                "createdAt": "2026-03-25T10:00:00Z"
              },
              "message": "Review submitted successfully"
            }""";

    public static final String GET_REVIEWS_BY_DRIVER_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "reviewId": "review_001",
                  "tripId": "trip_001",
                  "reviewerId": "user_123",
                  "reviewerRole": "PASSENGER",
                  "subjectId": "driver_001",
                  "subjectRole": "DRIVER",
                  "rating": 5,
                  "comment": "Great driver!",
                  "status": "PUBLISHED",
                  "createdAt": "2026-03-25T10:00:00Z"
                }
              ],
              "message": "Reviews retrieved successfully"
            }""";

    public static final String GET_AVERAGE_RATING_RESPONSE = """
            {
              "success": true,
              "data": {
                "subjectId": "driver_001",
                "averageRating": 4.75,
                "totalReviews": 4
              },
              "message": "Average rating retrieved successfully"
            }""";

    // ---- Errors ----

    public static final String ERROR_REVIEW_ALREADY_EXISTS = """
            {
              "status": 409,
              "code": "REVIEW_ALREADY_EXISTS",
              "message": "A review for this trip already exists",
              "error": {
                "path": "/api/reviews",
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
                "path": "/api/reviews",
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
                "path": "/api/reviews",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12353"
            }""";

    public static final String ERROR_INVALID_RATING = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid review data provided",
              "error": {
                "path": "/api/reviews",
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
}