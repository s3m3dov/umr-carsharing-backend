package com.cimhub.notificationservice.dto.examples;

public final class NotificationApiDoc {

    private NotificationApiDoc() {}

    public static final String CREATE_NOTIFICATION_REQUEST = """
            {
              "recipientId": "jane.passenger@example.com",
              "senderId": "john.driver@example.com",
              "title": "Booking Confirmed",
              "message": "Your booking for trip trip_001 is confirmed.",
              "type": "BOOKING_REQUESTED",
              "relatedEntityId": "booking_001",
              "metadata": {
                "tripId": "trip_001",
                "bookingId": "booking_001"
              }
            }""";

    public static final String MARK_READ_REQUEST = """
            {
              "recipientId": "jane.passenger@example.com"
            }""";

    public static final String CREATE_NOTIFICATION_RESPONSE = """
            {
              "data": {
                "notificationId": "not_001",
                "recipientId": "jane.passenger@example.com",
                "senderId": "john.driver@example.com",
                "title": "Booking Confirmed",
                "message": "Your booking for trip trip_001 is confirmed.",
                "type": "BOOKING_REQUESTED",
                "relatedEntityId": "booking_001",
                "metadata": {
                  "tripId": "trip_001",
                  "bookingId": "booking_001"
                },
                "read": false,
                "readAt": null,
                "createdAt": "2026-03-30T11:35:21.111Z",
                "updatedAt": "2026-03-30T11:35:21.111Z"
              },
              "message": "Operation successful",
              "timestamp": "2026-03-30T11:35:21.111Z"
            }""";

    public static final String NOTIFICATION_STATS_RESPONSE = """
            {
              "data": {
                "recipientId": "jane.passenger@example.com",
                "unreadCount": 3
              },
              "message": "Operation successful",
              "timestamp": "2026-03-30T11:35:21.111Z"
            }""";

    public static final String ERROR_NOTIFICATION_NOT_FOUND = """
            {
              "status": 404,
              "code": "NOTIFICATION_NOT_FOUND",
              "message": "Notification not found: not_001",
              "error": {
                "path": "/api/notifications/not_001/read",
                "details": []
              },
              "timestamp": "2026-03-30T11:35:21.111Z",
              "requestId": "req_12345"
            }""";

    public static final String ERROR_UNAUTHORIZED = """
            {
              "status": 403,
              "code": "UNAUTHORIZED_NOTIFICATION_ACCESS",
              "message": "You are not allowed to access notifications for this user",
              "error": {
                "path": "/api/notifications/john.driver@example.com",
                "details": []
              },
              "timestamp": "2026-03-30T11:35:21.111Z",
              "requestId": "req_12346"
            }""";

    public static final String ERROR_INVALID_SENDER = """
            {
              "status": 400,
              "code": "INVALID_NOTIFICATION_SENDER",
              "message": "senderId and recipientId cannot be the same",
              "error": {
                "path": "/api/notifications",
                "details": []
              },
              "timestamp": "2026-03-30T11:35:21.111Z",
              "requestId": "req_12347"
            }""";
}