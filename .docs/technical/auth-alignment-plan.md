# Authentication & User Profile Alignment Plan

This document outlines the architectural decision to utilize our internal **JWT-based Authentication System** (Backend Auth) instead of Firebase, while grounding the mobile team's data and functional requirements into our existing microservices.

## 1. Architectural Decisions

### 1.1 Internal Identity Provider
* **Decision**: We will NOT use Firebase. The `auth-service` will remain the primary Identity Provider (IdP).
* **Rationale**: 
    * The backend already has a functional Spring Security + JWT implementation.
    * Full control over user data and security policies.
    * Zero dependency on external third-party services for core authentication.
    * Reduced architectural complexity (no dual-database synchronization).

### 1.2 User Profile Source of Truth
* **Decision**: The `user-service` will manage all profile data, including the new fields requested by the mobile team.
* **Database**: Continue using **MongoDB** (distributed per service) for high performance and schema flexibility.

### 1.3 Service Responsibilities
* **Auth Service**: Handle `/api/auth/signup`, `/api/auth/login`, and token issuance.
* **User Service**: Manage extended profiles (driver details, vehicle preferences).
* **API Gateway**: Enforce JWT validation and inject headers (`X-User-Id`, `X-User-Role`) for downstream services.

## 2. Data Model Alignment

The `User` model in `user-service` and the `RegisterUserRequest` in `auth-service` will be updated to support the mobile team's form fields.

| Mobile Requirement | Backend Implementation | Status |
|-------------------|------------------------|--------|
| `name`            | Map to `firstName`      | Update DTO |
| `surname`         | Map to `lastName`      | Update DTO |
| `role`            | Map to `UserType`      | Align Enums |
| `phone`           | `phoneNumber`          | Existing |
| `age`             | `age`                  | Existing |
| `car_model`       | Add to `vehicleInfo`   | New Field |
| `license_number`  | Add to `vehicleInfo`    | New Field |

For user: add firstName, and lastName
Choose better names for car-model and license-number


### 2.1 Role Mapping
Mobile team's requested roles will map to our existing `UserType` enum:
* `user` → `PASSENGER`
* `driver` → `DRIVER`


## 3. API Grounding

### 3.1 Updated Endpoints

#### `POST /api/auth/signup`
* **Input**: Full profile data + password.
* **Action**: Create auth credentials in `auth-service`, then create profile in `user-service`.
* **Response**: Internal JWT token.

#### `POST /api/auth/login`
* **Input**: Email + Password.
* **Action**: Verify credentials, return JWT + User Role (to help App decide which dashboard to show).

### 3.2 Authenticated Requests
* The mobile app must send the `Authorization: Bearer <JWT>` header.
* The API Gateway extracts the user identity and role, passing them downstream.

## 4. Mobile Team Integration Guide

To align with this backend strategy, the Flutter app needs to:
1.  **Replace Firebase SDK**: Use standard HTTP clients (Dio/Http) to call `/api/auth/login` and `/api/auth/signup`.
2.  **Token Persistence**: Store the backend-issued JWT in secure storage (e.g., `flutter_secure_storage`).
3.  **Role Handling**: Read the `role` from the login response to navigate to either the Driver or Passenger dashboard.
