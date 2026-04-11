package com.cimhub.userservice.vehicle.dto.examples;

public final class VehicleApiDoc {

    private VehicleApiDoc() {}

    public static final String REGISTER_VEHICLE_REQUEST = """
            {
              "userId": "john.driver@example.com",
              "vehicleName": "Tesla Model 3",
              "vehicleNumber": "HE-XX-1234",
              "vehicleType": "sedan",
              "vehicleColor": "white",
              "seatingCapacity": "5"
            }""";

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
              "data": {
                "value": "HE-XX-1234",
                "text": "Tesla Model 3 | white",
                "seatingCapacity": "5"
              },
              "message": "Vehicle registered successfully"
            }""";

    public static final String ERROR_VEHICLE_EXISTS = """
            {
              "status": 400,
              "code": "BAD_REQUEST",
              "message": "Vehicle already registered",
              "error": {
                "path": "/api/vehicles/register",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12354"
            }""";

    public static final String ERROR_DRIVER_HAS_VEHICLE = """
            {
              "status": 400,
              "code": "BAD_REQUEST",
              "message": "Driver already has a vehicle registered",
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
              "message": "No vehicles registered for user john.driver@example.com",
              "error": {
                "path": "/api/vehicles/john.driver@example.com",
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

    // ---- Admin Responses ----

    public static final String ADMIN_LIST_VEHICLES_RESPONSE = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "id": "vehicle_001",
                    "userId": "john.driver@example.com",
                    "vehicleNumber": "HE-XX-1234",
                    "vehicleName": "Tesla Model 3",
                    "vehicleType": "sedan",
                    "vehicleColor": "white",
                    "seatingCapacity": "5",
                    "createdAt": "2026-03-30T12:00:00.000Z",
                    "updatedAt": "2026-03-30T12:05:45.456Z"
                  }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
              },
              "message": "Vehicles retrieved successfully"
            }""";

    public static final String ADMIN_GET_VEHICLE_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": "vehicle_001",
                "userId": "john.driver@example.com",
                "vehicleNumber": "HE-XX-1234",
                "vehicleName": "Tesla Model 3",
                "vehicleType": "sedan",
                "vehicleColor": "white",
                "seatingCapacity": "5",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T12:05:45.456Z"
              },
              "message": "Vehicle retrieved successfully"
            }""";

    public static final String ADMIN_CREATE_VEHICLE_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": "vehicle_001",
                "userId": "john.driver@example.com",
                "vehicleNumber": "HE-XX-1234",
                "vehicleName": "Tesla Model 3",
                "vehicleType": "sedan",
                "vehicleColor": "white",
                "seatingCapacity": "5",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T12:00:00.000Z"
              },
              "message": "Vehicle created successfully"
            }""";

    public static final String ADMIN_UPDATE_VEHICLE_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": "vehicle_001",
                "userId": "john.driver@example.com",
                "vehicleNumber": "HE-XX-1234",
                "vehicleName": "Tesla Model 3",
                "vehicleType": "sedan",
                "vehicleColor": "white",
                "seatingCapacity": "5",
                "createdAt": "2026-03-30T12:00:00.000Z",
                "updatedAt": "2026-03-30T12:10:30.123Z"
              },
              "message": "Vehicle updated successfully"
            }""";

    public static final String ADMIN_DELETE_VEHICLE_RESPONSE = """
            {
              "success": true,
              "data": "Vehicle deleted successfully",
              "message": "Vehicle deleted"
            }""";

    public static final String ERROR_ADMIN_UNAUTHORIZED = """
            {
              "status": 403,
              "code": "UNAUTHORIZED",
              "message": "Authentication required",
              "error": {
                "path": "/api/admin/vehicles",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12390"
            }""";

    public static final String ERROR_VEHICLE_NOT_FOUND = """
            {
              "status": 404,
              "code": "VEHICLE_NOT_FOUND",
              "message": "Vehicle vehicle_001 not found",
              "error": {
                "path": "/api/admin/vehicles/vehicle_001",
                "details": []
              },
              "timestamp": "2026-03-25T15:22:46.870Z",
              "requestId": "req_12391"
            }""";
}
