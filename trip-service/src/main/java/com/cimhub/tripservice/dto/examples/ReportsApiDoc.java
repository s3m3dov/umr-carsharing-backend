package com.cimhub.tripservice.dto.examples;

/**
 * OpenAPI example payloads for Reports endpoints.
 * Referenced by {@link io.swagger.v3.oas.annotations.media.Schema} annotations.
 */
public final class ReportsApiDoc {

    private ReportsApiDoc() {}

    // ---- Responses ----

    public static final String DRIVER_PERFORMANCE_REPORT = """
            {
              "success": true,
              "data": {
                "totalDrivers": 25,
                "totalTrips": 150,
                "averageRating": 4.7,
                "topDrivers": [
                  {
                    "driverId": "john.driver@example.com",
                    "tripsCompleted": 45,
                    "averageRating": 4.9,
                    "totalRevenue": 2500.00
                  }
                ]
              },
              "message": "Driver performance report generated"
            }""";

    public static final String BOOKING_SUMMARY_REPORT = """
            {
              "success": true,
              "data": {
                "totalBookings": 200,
                "confirmedBookings": 180,
                "cancelledBookings": 20,
                "completionRate": 90.0
              },
              "message": "Booking summary report generated"
            }""";

    public static final String REVENUE_REPORT = """
            {
              "success": true,
              "data": {
                "totalRevenue": 15000.00,
                "byMonth": [
                  {
                    "month": "2026-03",
                    "revenue": 5000.00
                  }
                ]
              },
              "message": "Revenue report generated"
            }""";

    public static final String PASSENGER_ACTIVITY_REPORT = """
            {
              "success": true,
              "data": {
                "totalPassengers": 100,
                "activePassengers": 75,
                "tripsByPassenger": [
                  {
                    "passengerId": "jane.passenger@example.com",
                    "tripsCompleted": 15,
                    "totalSpent": 250.00
                  }
                ]
              },
              "message": "Passenger activity report generated"
            }""";

    public static final String AUDIT_LOGS_RESPONSE = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "id": "audit_001",
                    "action": "TRIP_CREATED",
                    "entityType": "TRIP",
                    "entityId": "trip_001",
                    "performedBy": "john.driver@example.com",
                    "timestamp": "2026-03-30T12:00:00.000Z",
                    "details": {
                      "source": "50.8154,8.6747",
                      "destination": "50.7265,7.0895"
                    }
                  }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
              },
              "message": "Audit logs retrieved"
            }""";

    public static final String AUDIT_LOG_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": "audit_001",
                "action": "TRIP_CREATED",
                "entityType": "TRIP",
                "entityId": "trip_001",
                "performedBy": "john.driver@example.com",
                "timestamp": "2026-03-30T12:00:00.000Z",
                "details": {
                  "source": "50.8154,8.6747",
                  "destination": "50.7265,7.0895"
                }
              },
              "message": "Audit log retrieved"
            }""";

    // ---- Errors ----

    public static final String ERROR_AUDIT_LOG_NOT_FOUND = """
            {
              "status": 404,
              "code": "AUDIT_LOG_NOT_FOUND",
              "message": "Audit log audit_001 not found",
              "error": {
                "path": "/api/admin/reports/audit-logs/audit_001",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12371"
            }""";
}
