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
              "success": false,
              "error": "VEHICLE_ALREADY_EXISTS",
              "message": "Vehicle with license plate HE-XX-1234 already registered"
            }""";

    public static final String ERROR_VEHICLE_LIMIT = """
            {
              "success": false,
              "error": "VEHICLE_LIMIT_EXCEEDED",
              "message": "Maximum 5 vehicles can be registered per driver"
            }""";

    public static final String ERROR_NO_VEHICLES = """
            {
              "success": false,
              "error": "NO_VEHICLES_FOUND",
              "message": "No vehicles registered for user user_123"
            }""";
}
