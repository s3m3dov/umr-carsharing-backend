package com.cimhub.vehicleservice.controller;

/**
 * OpenAPI example payloads for Vehicle Service endpoints.
 * Referenced by {@link VehicleApi} annotations.
 */
public final class VehicleApiExamples {

    private VehicleApiExamples() {}

    // ---- Requests ----

    public static final String REGISTER_VEHICLE_REQUEST = """
            {
              "userId": "user_123",
              "vehicleName": "Tesla Model 3",
              "vehicleNumber": "HE-XX-1234",
              "vehicleType": "sedan",
              "vehicleColor": "white",
              "seatingCapacity": "5"
            }""";

    // ---- Responses ----

    public static final String GET_VEHICLES_RESPONSE = """
            {
              "success": true,
              "data": [
                {
                  "value": "vehicle_001",
                  "text": "Tesla Model 3 - HE-XX-1234",
                  "seatingCapacity": "5"
                },
                {
                  "value": "vehicle_002",
                  "text": "BMW 3 Series - HE-XX-5678",
                  "seatingCapacity": "5"
                }
              ],
              "message": "Vehicles retrieved successfully"
            }""";

    public static final String REGISTER_VEHICLE_RESPONSE = """
            {
              "success": true,
              "data": null,
              "message": "Vehicle registered successfully"
            }""";

    // ---- Errors ----

    public static final String ERROR_VEHICLE_EXISTS = """
            {
              "status": 409,
              "code": "VEHICLE_ALREADY_EXISTS",
              "message": "Vehicle with license plate HE-XX-1234 already registered",
              "error": {
                "path": "/api/vehicles/register",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12354"
            }""";

    public static final String ERROR_VEHICLE_LIMIT = """
            {
              "status": 409,
              "code": "VEHICLE_LIMIT_EXCEEDED",
              "message": "Maximum 5 vehicles can be registered per driver",
              "error": {
                "path": "/api/vehicles/register",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12355"
            }""";

    public static final String ERROR_NO_VEHICLES = """
            {
              "status": 404,
              "code": "NO_VEHICLES_FOUND",
              "message": "No vehicles registered for user user_123",
              "error": {
                "path": "/api/vehicles/user_123",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12356"
            }""";

    public static final String ERROR_INVALID_VEHICLE_DATA = """
            {
              "status": 400,
              "code": "VALIDATION_ERROR",
              "message": "Invalid vehicle data provided",
              "error": {
                "path": "/api/vehicles/register",
                "details": [
                  {
                    "field": "vehicleNumber",
                    "message": "Vehicle number is required",
                    "type": "NotBlank"
                  },
                  {
                    "field": "seatingCapacity",
                    "message": "Seating capacity must be between 1 and 8",
                    "type": "Range"
                  }
                ]
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12364"
            }""";

    public static final String ERROR_VEHICLE_LIMIT = """
            {
              "status": 409,
              "code": "VEHICLE_LIMIT_EXCEEDED",
              "message": "Maximum 5 vehicles can be registered per driver",
              "error": {
                "path": "/api/vehicles",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12355"
            }""";

    public static final String ERROR_NO_VEHICLES = """
            {
              "status": 404,
              "code": "NO_VEHICLES_FOUND",
              "message": "No vehicles registered for user user_123",
              "error": {
                "path": "/api/vehicles/user/user_123",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12356"
            }""";
}
