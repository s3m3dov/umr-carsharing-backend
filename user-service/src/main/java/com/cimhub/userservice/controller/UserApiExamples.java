package com.cimhub.userservice.controller;

/**
 * OpenAPI example payloads for User Service endpoints.
 * Referenced by {@link UserApi} annotations.
 */
public final class UserApiExamples {

    private UserApiExamples() {}

    // ---- Requests ----

    public static final String CREATE_PROFILE_REQUEST = """
            {
              "userId": "john.driver@example.com",
              "firstName": "John",
              "lastName": "Driver",
              "phoneNumber": "+49 123 456789",
              "age": 32,
              "licenseNumber": "LD-123456789"
            }""";

    // ---- Responses ----

    public static final String GET_USER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "role": "DRIVER",
                "profileCompletedAt": "2024-01-15T10:30:00Z"
              },
              "message": "User profile retrieved successfully"
            }""";

    public static final String CREATE_PROFILE_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "john.driver@example.com",
                "firstName": "John",
                "lastName": "Driver",
                "email": "john.driver@example.com",
                "phoneNumber": "+49 123 456789",
                "age": 32,
                "role": "DRIVER",
                "profileCompletedAt": "2024-01-15T10:35:00Z"
              },
              "message": "User profile created successfully"
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

    public static final String ERROR_INVALID_DATA = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid user data provided",
              "error": {
                "path": "/api/users/profile",
                "details": [
                  {
                    "field": "phoneNumber",
                    "message": "Phone number is required",
                    "type": "NotBlank"
                  },
                  {
                    "field": "age",
                    "message": "Age must be at least 18",
                    "type": "Min"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12350"
            }""";
}
