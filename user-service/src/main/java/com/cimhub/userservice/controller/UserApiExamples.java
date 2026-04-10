package com.cimhub.userservice.controller;

/**
 * OpenAPI example payloads for User Service endpoints.
 * Referenced by {@link UserApi} annotations.
 */
public final class UserApiExamples {

    private UserApiExamples() {}

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

}
