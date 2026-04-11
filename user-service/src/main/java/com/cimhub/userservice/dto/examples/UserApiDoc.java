package com.cimhub.userservice.dto.examples;

/**
 * OpenAPI example payloads for User Service endpoints.
 * Referenced by {@link io.swagger.v3.oas.annotations.media.Schema} annotations.
 */
public final class UserApiDoc {

    private UserApiDoc() {}

    // ---- Responses ----

    public static final String GET_USER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "userType": "DRIVER",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "licenseNumber": "LD-123456789",
                "createdAt": "2026-03-30T16:48:24.233Z",
                "updatedAt": "2026-03-30T16:48:24.233Z"
              },
              "message": "Operation successful"
            }""";

    // ---- Errors ----

    public static final String ERROR_USER_NOT_FOUND = """
            {
              "status": 404,
              "code": "USER_NOT_FOUND",
              "message": "User with ID john.driver@example.com not found",
              "error": {
                "path": "/api/users/john.driver@example.com",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12349"
            }""";

    // ---- Admin Driver Responses ----

    public static final String ADMIN_LIST_DRIVERS_RESPONSE = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "userId": "john.driver@example.com",
                    "firstName": "John",
                    "lastName": "Driver",
                    "email": "john.driver@example.com",
                    "phoneNumber": "+49 123 456789",
                    "age": 32,
                    "licenseNumber": "LD-123456789",
                    "driverStatus": "APPROVED",
                    "createdAt": "2026-03-30T12:00:00.000Z",
                    "updatedAt": "2026-03-30T12:00:00.000Z"
                  }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
              },
              "message": "Drivers retrieved successfully"
            }""";

    public static final String ADMIN_GET_DRIVER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "licenseNumber": "LD-123456789",
                "driverStatus": "APPROVED",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T12:00:00.000Z"
              },
              "message": "Driver retrieved successfully"
            }""";

    public static final String ADMIN_APPROVE_DRIVER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "licenseNumber": "LD-123456789",
                "driverStatus": "APPROVED",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T14:00:00.000Z"
              },
              "message": "Driver approved successfully"
            }""";

    public static final String ADMIN_REJECT_DRIVER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "licenseNumber": "LD-123456789",
                "driverStatus": "REJECTED",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T14:00:00.000Z"
              },
              "message": "Driver rejected successfully"
            }""";

    public static final String ADMIN_UPDATE_DRIVER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "licenseNumber": "LD-123456789",
                "driverStatus": "APPROVED",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T15:00:00.000Z"
              },
              "message": "Driver updated successfully"
            }""";

    public static final String ADMIN_DELETE_DRIVER_RESPONSE = """
            {
              "success": true,
              "data": "Driver deleted successfully",
              "message": "Driver deleted"
            }""";

    public static final String ADMIN_DRIVER_DOCUMENTS_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "documentType": "DRIVERS_LICENSE",
                  "documentUrl": "https://storage.example.com/documents/license_001.pdf",
                  "uploadedAt": "2026-03-30T12:00:00.000Z"
                }
              ],
              "message": "Driver documents retrieved successfully"
            }""";

    // ---- Admin Passenger Responses ----

    public static final String ADMIN_LIST_PASSENGERS_RESPONSE = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "userId": "jane.passenger@example.com",
                    "firstName": "Jane",
                    "lastName": "Passenger",
                    "email": "jane.passenger@example.com",
                    "phoneNumber": "+49 111 222333",
                    "age": 27,
                    "createdAt": "2026-03-30T12:00:00.000Z",
                    "updatedAt": "2026-03-30T12:00:00.000Z"
                  }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
              },
              "message": "Passengers retrieved successfully"
            }""";

    public static final String ADMIN_GET_PASSENGER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "jane.passenger@example.com",
                "firstName": "Jane",
                "lastName": "Passenger",
                "email": "jane.passenger@example.com",
                "phoneNumber": "+49 111 222333",
                "age": 27,
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T12:00:00.000Z"
              },
              "message": "Passenger retrieved successfully"
            }""";

    public static final String ADMIN_CREATE_PASSENGER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "jane.passenger@example.com",
                "firstName": "Jane",
                "lastName": "Passenger",
                "email": "jane.passenger@example.com",
                "phoneNumber": "+49 111 222333",
                "age": 27,
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T12:00:00.000Z"
              },
              "message": "Passenger created successfully"
            }""";

    public static final String ADMIN_UPDATE_PASSENGER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "jane.passenger@example.com",
                "firstName": "Jane",
                "lastName": "Passenger",
                "email": "jane.passenger@example.com",
                "phoneNumber": "+49 111 222333",
                "age": 27,
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T15:00:00.000Z"
              },
              "message": "Passenger updated successfully"
            }""";

    public static final String ADMIN_DELETE_PASSENGER_RESPONSE = """
            {
              "success": true,
              "data": "Passenger deleted successfully",
              "message": "Passenger deleted"
            }""";

    // ---- Bulk Actions ----

    public static final String BULK_DRIVER_ACTION_REQUEST = """
            {
              "driverIds": ["driver1@example.com", "driver2@example.com"],
              "reason": "Documentation complete"
            }""";

    public static final String BULK_APPROVE_RESPONSE = """
            {
              "success": true,
              "data": "2 drivers approved",
              "message": "Operation successful"
            }""";

    public static final String BULK_REJECT_RESPONSE = """
            {
              "success": true,
              "data": "2 drivers rejected",
              "message": "Operation successful"
            }""";

    // ---- Errors ----

    public static final String ERROR_DRIVER_NOT_FOUND = """
            {
              "status": 404,
              "code": "DRIVER_NOT_FOUND",
              "message": "Driver john.driver@example.com not found",
              "error": {
                "path": "/api/admin/drivers/john.driver@example.com",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12400"
            }""";

    public static final String ERROR_PASSENGER_NOT_FOUND = """
            {
              "status": 404,
              "code": "PASSENGER_NOT_FOUND",
              "message": "Passenger jane.passenger@example.com not found",
              "error": {
                "path": "/api/admin/passengers/jane.passenger@example.com",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12401"
            }""";

    public static final String ERROR_ADMIN_UNAUTHORIZED = """
            {
              "status": 403,
              "code": "UNAUTHORIZED",
              "message": "Authentication required",
              "error": {
                "path": "/api/admin/drivers",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12402"
            }""";

    public static final String ERROR_INVALID_REQUEST = """
            {
              "status": 400,
              "code": "BAD_REQUEST",
              "message": "Missing or invalid request data",
              "error": {
                "path": "/api/admin/profile",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12403"
            }""";

    public static final String ADMIN_PROFILE_RESPONSE = """
            {
              "success": true,
              "data": {
                "email": "admin@example.com",
                "role": "ADMIN"
              },
              "message": "Operation successful"
            }""";
}
