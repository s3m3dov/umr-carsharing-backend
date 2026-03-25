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
              "licenseNumber": "LD-123456789"
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
              "status": 401,
              "code": "INVALID_CREDENTIALS",
              "message": "Invalid email or password",
              "error": {
                "path": "/api/auth/login",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12345"
            }""";

    public static final String ERROR_USER_EXISTS = """
            {
              "status": 409,
              "code": "USER_ALREADY_EXISTS",
              "message": "User with email john.driver@example.com already exists",
              "error": {
                "path": "/api/auth/signup",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12346"
            }""";

    public static final String ERROR_VALIDATION = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid input data",
              "error": {
                "path": "/api/auth/signup",
                "details": [
                  {
                    "field": "email",
                    "message": "Email is required",
                    "type": "NotBlank"
                  },
                  {
                    "field": "password",
                    "message": "Password must be at least 8 characters",
                    "type": "Size"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12347"
            }""";

    public static final String ERROR_INVALID_TOKEN = """
            {
              "status": 401,
              "code": "INVALID_TOKEN",
              "message": "Invalid or expired token",
              "error": {
                "path": "/api/auth/validate",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12348"
            }""";
}
