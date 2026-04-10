# User and Profile Architecture

This document defines the architecture for user identity, base profile data, and role-specific profiles (driver/passenger) across `auth-service` and `user-service`.

It is the source of truth for ownership boundaries, data models, API contracts, lifecycle flows, and migration strategy.

## 1. Goals and Scope

### Goals

- Keep authentication and profile concerns separated by service boundary.
- Make `auth-service` the only service responsible for credential lifecycle.
- Make `user-service` profile-oriented with explicit role sub-profiles.
- Prevent cross-service data corruption from shared document writes.
- Keep signup flow simple for clients: one call creates account + profile baseline.

### In Scope

- User identity and profile data ownership
- MongoDB collection model and indexes
- Signup/login/profile flows
- Driver/passenger profile lifecycle
- Consistency and rollback strategy
- Security and authorization boundaries
- Migration from shared `users` documents

### Out of Scope

- Trip, booking, vehicle, and review domain internals
- Frontend/mobile UX details
- External IdP integrations (Firebase, OAuth)

## 2. Architecture Principles

1. Single writer per aggregate
   - Each aggregate has one owning service that performs writes.
2. Separate security data from business profile data
   - Password hashes and reset tokens are never stored in profile collections.
3. Stable identity key
   - `userId` (email in current implementation) links records across collections.
4. Explicit role specialization
   - Driver-specific and passenger-specific fields live in dedicated collections.
5. Safe retries
   - Profile bootstrap APIs must be idempotent for signup retries.

## 3. Service Responsibilities

## Auth Service (`/auth-service`)

- Owns credentials and security state.
- Handles signup/login/password reset/change.
- Issues JWT with role claims.
- Orchestrates initial role sub-profile creation in `user-service`.

## User Service (`/user-service`)

- Owns base user profile (non-credential PII).
- Owns role-specific profile collections (`drivers`, `passengers`).
- Handles profile reads and profile updates.
- Handles admin profile moderation/management.

## API Gateway

- Validates JWT through auth flow.
- Propagates identity/role headers (`X-User-Id`, `X-User-Role`, `X-User-Email`).
- Routes calls by service prefix.

## 4. Data Ownership and Collections

## Recommended target model

| Collection | Owner | Purpose | Key fields | Sensitive fields |
|---|---|---|---|---|
| `users` | auth-service | Authentication identity | `userId`, `email`, `userType` | `passwordHash`, `resetToken`, `resetTokenExpiry` |
| `drivers` | user-service | Driver full profile | `userId`, `firstName`, `lastName`, `email`, `phoneNumber`, `age`, `licenseNumber`, `driverStatus` | PII only |
| `passengers` | user-service | Passenger full profile | `userId`, `firstName`, `lastName`, `email`, `phoneNumber`, `age` | PII only |

## Mandatory indexes

- `users.email` unique
- `drivers.userId` unique
- `passengers.userId` unique

## Current transition state (important)

Auth credentials are stored only in `users` (auth-service). Profile documents are stored only in role collections (`drivers`, `passengers`) in user-service.

## 5. Canonical Data Schemas

## `users` auth document

```json
{
  "_id": "john.driver@example.com",
  "userId": "john.driver@example.com",
  "email": "john.driver@example.com",
  "userType": "DRIVER",
  "passwordHash": "$2a$10$...",
  "resetToken": null,
  "resetTokenExpiry": null,
  "createdAt": "2026-04-09T10:15:00Z",
  "updatedAt": "2026-04-09T10:15:00Z"
}
```

## `drivers` document

```json
{
  "_id": "drv_6f4...",
  "userId": "john.driver@example.com",
  "firstName": "John",
  "lastName": "Driver",
  "email": "john.driver@example.com",
  "phoneNumber": "+49 123 456789",
  "age": 32,
  "licenseNumber": "LD-123456789",
  "driverStatus": "PENDING",
  "createdAt": "2026-04-09T10:15:01Z",
  "updatedAt": "2026-04-09T10:15:01Z",
  "createdBy": "john.driver@example.com"
}
```

## `passengers` document

```json
{
  "_id": "psg_7a1...",
  "userId": "jane.passenger@example.com",
  "firstName": "Jane",
  "lastName": "Passenger",
  "email": "jane.passenger@example.com",
  "phoneNumber": "+49 555 000111",
  "age": 27,
  "createdAt": "2026-04-09T10:15:01Z",
  "updatedAt": "2026-04-09T10:15:01Z",
  "createdBy": "jane.passenger@example.com"
}
```

## 6. API Contracts

All public APIs are called through gateway prefixes:

- Auth: `/auth-service/api/auth/*`
- User: `/user-service/api/*`

## Auth endpoints

- `POST /api/auth/signup`
  - Creates auth identity in `users`.
  - Orchestrates role profile bootstrap in user-service.
- `POST /api/auth/login`
  - Returns JWT with user role.
- `POST /api/auth/validate`
  - Token validation (primarily internal).
- `POST /api/auth/reset-password`
- `POST /api/auth/reset-password/confirm`
- `PUT /api/auth/change-password`

## User and role profile endpoints

- `GET /api/users/{userId}`
  - Aggregated profile read from `drivers` or `passengers`.
- `POST /api/drivers/{userId}`
  - Create driver sub-profile.
- `GET /api/drivers/{userId}`
  - Driver profile read.
- `PUT /api/drivers/{userId}`
  - Driver profile update.
- `POST /api/passengers/{userId}`
  - Create passenger sub-profile.
- `GET /api/passengers/{userId}`
  - Passenger profile read.
- `PUT /api/passengers/{userId}`
  - Passenger profile update.

## Admin profile endpoints

- `/api/admin/drivers/*`
- `/api/admin/passengers/*`

These are profile-management APIs. They must not become a second source of truth for credential creation.

## 7. Lifecycle Flows

## 7.1 Driver signup

1. Client calls `POST /api/auth/signup` with `userType=DRIVER` and `licenseNumber`.
2. Auth service validates request and writes auth record in `users`.
3. Auth service calls user-service `POST /api/drivers/{userId}`.
4. User-service creates `drivers` row with full profile data.
5. On success, auth-service returns JWT.
6. On failure, auth-service compensates by deleting newly created auth identity.

## 7.2 Passenger signup

1. Client calls `POST /api/auth/signup` with `userType=PASSENGER`.
2. Auth service writes auth record in `users`.
3. Auth service calls user-service `POST /api/passengers/{userId}`.
4. User-service creates passenger profile row.
5. Auth service returns JWT; on failure performs rollback.

## 7.3 Profile updates

1. Client calls passenger or driver `PUT` endpoint.
2. User-service updates only profile-owned fields.
3. Auth fields are never touched by user-service writes.

## 8. Consistency and Failure Handling

## Required behavior

- Signup orchestration is synchronous with compensation.
- If sub-profile creation fails, auth identity creation is rolled back.
- `POST /api/drivers/{userId}` and `POST /api/passengers/{userId}` should be idempotent for retry scenarios.

## Recommended hardening

- Introduce correlation/request ID across auth -> user calls.
- Emit audit records for creation and compensation.
- Optionally introduce outbox/eventing if signup throughput or resilience requirements increase.

## 9. Validation and Business Rules

- `email` is normalized to lowercase on write.
- `userType` is immutable after account creation.
- Driver signup requires non-empty `licenseNumber`.
- Passenger signup must not accept driver-only fields.
- `userId` and `email` remain aligned unless a dedicated email-change workflow is introduced.

## 10. Security and Access Control

- Credentials only in auth-service `users` collection.
- JWT is the only accepted auth token for service APIs.
- API gateway validates token and forwards identity headers.
- Admin endpoints require `X-User-Role=ADMIN` enforcement.
- Profile endpoints enforce role constraints (`DRIVER` for driver profile APIs, `PASSENGER` for passenger APIs).

## 11. Migration Plan: User base profile -> role profile collections (clean break)

1. Stop user-service writes to any shared/base `users` profile model.
2. Require full profile data on `POST /api/drivers/{userId}` and `POST /api/passengers/{userId}`.
3. Backfill legacy user-service profile docs into `drivers` or `passengers` by `userType`.
4. Drop legacy user-service `users` profile collection after verification.

## 12. Testing Requirements

Minimum required test coverage for this architecture:

- Signup success for DRIVER and PASSENGER
- Signup rollback on user-service sub-profile failure
- Login still works after profile updates
- Driver/passenger role mismatch returns 404/forbidden contract
- Idempotent sub-profile create retry behavior
- Admin profile updates do not affect auth credentials

## 13. Operational Checklist

Before release:

- Verify indexes exist in all collections.
- Verify no user-service code path reads/writes auth-service `users` documents.
- Verify all docs/examples avoid the deprecated profile-creation endpoint from the old flow.
- Verify audit logs capture create/update/delete/rollback actions.

## 14. Current Code References

- Auth controller: `auth-service/src/main/java/com/cimhub/authservice/controller/AuthController.java`
- Auth signup orchestration: `auth-service/src/main/java/com/cimhub/authservice/service/AuthService.java`
- Auth -> user client: `auth-service/src/main/java/com/cimhub/authservice/service/UserServiceClient.java`
- Base user read: `user-service/src/main/java/com/cimhub/userservice/controller/UserController.java`
- Driver profile APIs: `user-service/src/main/java/com/cimhub/userservice/controller/DriverController.java`
- Passenger profile APIs: `user-service/src/main/java/com/cimhub/userservice/controller/PassengerController.java`
- Driver profile service: `user-service/src/main/java/com/cimhub/userservice/service/DriverService.java`
- Passenger profile service: `user-service/src/main/java/com/cimhub/userservice/service/PassengerService.java`
