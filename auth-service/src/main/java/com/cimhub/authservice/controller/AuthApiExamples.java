package com.cimhub.authservice.controller;

/**
 * OpenAPI example payloads for Auth Service endpoints.
 * Referenced by {@link AuthApi} annotations.
 */
public final class AuthApiExamples {

    private AuthApiExamples() {}

    // ---- Requests ----

    public static final String LOGIN_REQUEST = """
            {
              "email": "john.driver@example.com",
              "password": "SecurePassword123!"
            }""";

    public static final String SIGNUP_REQUEST = """
            {
              "firstName": "John",
              "lastName": "Driver",
              "email": "john.driver@example.com",
              "password": "SecurePassword123!",
              "userType": "DRIVER",
              "phoneNumber": "+49 123 456789",
              "age": 32,
              "vehicleModel": "Tesla Model 3",
              "licensePlate": "HE-XX-1234"
            }""";

    public static final String VALIDATE_TOKEN_REQUEST = """
            {
              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsInJvbGUiOiJEUklWRVIiLCJleHAiOjE2OTk4NzcyMDB9.signature"
            }""";

    // ---- Responses ----

    public static final String LOGIN_RESPONSE = """
            {
              "success": true,
              "data": {
                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsInJvbGUiOiJEUklWRVIiLCJleHAiOjE2OTk4NzcyMDB9.signature",
                "email": "john.driver@example.com",
                "role": "DRIVER"
              },
              "message": "Login successful"
            }""";

    public static final String SIGNUP_RESPONSE = """
            {
              "success": true,
              "data": {
                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsInJvbGUiOiJEUklWRVIiLCJleHAiOjE2OTk4NzcyMDB9.signature",
                "email": "john.driver@example.com",
                "role": "DRIVER"
              },
              "message": "User registered successfully"
            }""";

    public static final String VALIDATE_TOKEN_RESPONSE = """
            {
              "success": true,
              "data": {
                "isValid": true,
                "email": "john.driver@example.com",
                "role": "DRIVER"
              },
              "message": "Token is valid"
            }""";

    // ---- Errors ----

    public static final String ERROR_INVALID_CREDENTIALS = """
            {
              "success": false,
              "error": "INVALID_CREDENTIALS",
              "message": "Invalid email or password"
            }""";

    public static final String ERROR_USER_EXISTS = """
            {
              "success": false,
              "error": "USER_ALREADY_EXISTS",
              "message": "User with email john.driver@example.com already exists"
            }""";

    public static final String ERROR_VALIDATION = """
            {
              "success": false,
              "error": "VALIDATION_ERROR",
              "message": "Email is required"
            }""";

    public static final String ERROR_INVALID_TOKEN = """
            {
              "success": false,
              "error": "INVALID_TOKEN",
              "message": "Invalid or expired token"
            }""";
}
