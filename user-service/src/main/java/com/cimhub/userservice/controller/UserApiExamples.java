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
              "userId": "user_123",
              "firstName": "John",
              "lastName": "Driver",
              "phoneNumber": "+49 123 456789",
              "age": 32,
              "vehicleModel": "Tesla Model 3",
              "licensePlate": "HE-XX-1234"
            }""";

    // ---- Responses ----

    public static final String GET_USER_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": "user_123",
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
                "userId": "user_123",
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
              "success": false,
              "error": "USER_NOT_FOUND",
              "message": "User with ID user_123 not found"
            }""";

    public static final String ERROR_INVALID_DATA = """
            {
              "success": false,
              "error": "VALIDATION_ERROR",
              "message": "Invalid user data provided"
            }""";
}
