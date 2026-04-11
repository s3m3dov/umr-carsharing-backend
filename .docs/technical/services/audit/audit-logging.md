# Audit Logging

Audit logging is a cross-cutting concern implemented in the `common` module and shared across all services. Every significant state change — whether triggered by an admin, a user, or the system — is recorded as an immutable audit log entry in MongoDB.

---

## How It Works

1. A service calls a typed helper on `AuditService` (e.g., `logApprove`, `logCancel`).
2. `AuditService` builds an `AuditLog` document and persists it via `AuditLogRepository`.
3. If persistence fails, the error is logged but the original operation is **not rolled back** — audit failures are non-fatal by design.
4. The `serviceName` field is auto-populated from `spring.application.name`, so logs identify which service produced them.

---

## AuditService — Public API

All methods live in `common/src/main/java/com/cimhub/common/service/AuditService.java`.

| Method | AuditAction | Typical Use |
|--------|-------------|-------------|
| `logCreate(entityType, entityId, performedBy, details)` | CREATE | Entity created (user, vehicle, passenger) |
| `logUpdate(entityType, entityId, performedBy, details)` | UPDATE | Entity updated (profile, booking, password) |
| `logDelete(entityType, entityId, performedBy, details)` | DELETE | Entity deleted/removed |
| `logApprove(entityType, entityId, performedBy, details)` | APPROVE | Driver application approved |
| `logReject(entityType, entityId, performedBy, details)` | REJECT | Driver application rejected |
| `logFlag(entityType, entityId, performedBy, details)` | FLAG | Review flagged by admin |
| `logCancel(entityType, entityId, performedBy, details)` | CANCEL | Trip or booking cancelled |
| `logBook(entityType, entityId, performedBy, details)` | BOOK | Booking request submitted |
| `logPublish(entityType, entityId, performedBy, details)` | PUBLISH | Review published/restored |
| `logLogin(entityType, entityId, performedBy, details)` | LOGIN | Login attempt (success or failure) |

> **Note:** A private `logAction(AuditAction, ...)` method is the single implementation path. All public helpers delegate to it. Do not call `logAction` directly from outside `AuditService` — if no helper exists for an action, add one.

---

## AuditLog Document

**Collection:** `audit_logs`
**File:** `common/src/main/java/com/cimhub/common/model/AuditLog.java`

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB `_id`, auto-generated |
| `action` | AuditAction | Action performed |
| `entityType` | EntityType | Type of entity affected |
| `entityId` | String | ID of the affected entity |
| `performedBy` | String | Email or ID of the actor (admin, user, or `"SYSTEM"`) |
| `details` | String | Human-readable context or reason (nullable) |
| `serviceName` | String | Originating microservice (from `spring.application.name`) |
| `timestamp` | Instant | Auto-set to `Instant.now()` at creation time |

---

## AuditAction Enum

**File:** `common/src/main/java/com/cimhub/common/enums/AuditAction.java`

| Value | Description |
|-------|-------------|
| `CREATE` | Entity created |
| `UPDATE` | Entity modified |
| `DELETE` | Entity deleted |
| `APPROVE` | Application or request approved |
| `REJECT` | Application or request rejected |
| `FLAG` | Content flagged for review |
| `CANCEL` | Trip or booking cancelled |
| `PUBLISH` | Content published or restored |
| `RESTORE` | Entity restored (reserved) |
| `LOGIN` | Authentication event |
| `LOGOUT` | Logout event (reserved) |
| `BOOK` | Booking created |
| `COMPLETE` | Trip or booking completed (reserved) |
| `REVIEW` | Review submitted (reserved) |

---

## EntityType Enum

**File:** `common/src/main/java/com/cimhub/common/enums/EntityType.java`

`DRIVER` · `PASSENGER` · `VEHICLE` · `TRIP` · `BOOKING` · `REVIEW` · `USER` · `ADMIN` · `SYSTEM`

---

## AuditLogRepository

**File:** `common/src/main/java/com/cimhub/common/repository/AuditLogRepository.java`

Extends `MongoRepository<AuditLog, String>`. Custom query methods:

| Method | Description |
|--------|-------------|
| `findByEntityTypeAndEntityIdOrderByTimestampDesc(EntityType, String)` | Full history for a specific entity |
| `findByPerformedByOrderByTimestampDesc(String)` | All actions by a specific actor |
| `findByEntityTypeOrderByTimestampDesc(EntityType)` | All actions on a given entity type |

---

## Querying Audit Logs via API

The `ReportsController` in `trip-service` exposes a paginated, filterable audit log endpoint:

```
GET /api/admin/reports/audit-logs
```

Query parameters (all optional):

| Param | Type | Description |
|-------|------|-------------|
| `action` | AuditAction | Filter by action type |
| `entityType` | EntityType | Filter by entity type |
| `performedBy` | String | Filter by actor email/ID |
| `from` | Instant (ISO-8601) | Start of time range |
| `to` | Instant (ISO-8601) | End of time range |
| `page`, `size`, `sort` | Pageable | Pagination and sorting |

Returns `Page<AuditLogResponse>`.

```
GET /api/admin/reports/audit-logs/{id}
```

Returns a single `AuditLogResponse` by log ID.

**AuditLogResponse fields** (`trip-service/src/main/java/com/cimhub/tripservice/dto/response/AuditLogResponse.java`):
`id`, `action`, `entityType`, `entityId`, `performedBy`, `details`, `serviceName`, `timestamp`

---

## Audit Trail by Service

### auth-service — `AuthService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| User registered | USER | CREATE | "Auth account registered" |
| Auth account deleted | USER | DELETE | "Auth account deleted" |
| Login failed | USER | LOGIN | "User login failed" |
| Login succeeded | USER | LOGIN | "User login successful" |
| Password reset token generated | USER | UPDATE | "Password reset token generated" |
| Expired reset token cleared | USER | UPDATE | "Expired reset token cleared" |
| Password reset completed | USER | UPDATE | "Password reset completed" |
| Password changed by user | USER | UPDATE | "Password changed by user" |

### user-service — `UserService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| User profile created | DRIVER / PASSENGER | CREATE | "User profile created" |

### user-service — `AdminDriverService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Driver approved | DRIVER | APPROVE | "Driver approved" |
| Driver rejected | DRIVER | REJECT | "Driver rejected" |
| Driver info updated | DRIVER | UPDATE | "Driver info updated" |
| Driver deleted | DRIVER | DELETE | "Driver deleted" |

### user-service — `AdminPassengerService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Passenger created by admin | PASSENGER | CREATE | "Passenger created by admin" |
| Passenger info updated | PASSENGER | UPDATE | "Passenger info updated" |
| Passenger deleted | PASSENGER | DELETE | "Passenger deleted" |

### user-service — `VehicleService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Vehicle registered by user | VEHICLE | CREATE | "Vehicle registered by user" |

### user-service — `AdminVehicleService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Vehicle created by admin | VEHICLE | CREATE | "Vehicle created by admin" |
| Vehicle updated | VEHICLE | UPDATE | "Vehicle updated" |
| Vehicle deleted | VEHICLE | DELETE | "Vehicle deleted" |

### trip-service — `OfferRideService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Trip created by driver | TRIP | CREATE | "Trip created by driver" |

### trip-service — `JoinTripService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Booking request submitted | BOOKING | BOOK | "Booking request submitted by passenger" |
| Booking accepted by driver | BOOKING | APPROVE | "Booking accepted by driver" |
| Booking rejected by driver | BOOKING | REJECT | "Booking rejected by driver" |

### trip-service — `CancellationService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Booking cancelled by passenger | BOOKING | CANCEL | "Booking cancelled by passenger" |
| Booking cancelled (driver cancelled trip) | BOOKING | CANCEL | "Booking cancelled due to trip cancellation by driver" |
| Trip cancelled by driver | TRIP | CANCEL | "Trip cancelled by driver" |

### trip-service — `AdminBookingService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Booking updated by admin | BOOKING | UPDATE | "Booking updated by admin" |
| Booking cancelled by admin | BOOKING | CANCEL | "Booking cancelled by admin" |

### trip-service — `AdminTripService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Trip cancelled by admin | TRIP | CANCEL | "Trip cancelled by admin" |

### review-service — `ReviewService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Driver review submitted | REVIEW | CREATE | "Driver review submitted" |
| Passenger review submitted | REVIEW | CREATE | "Passenger review submitted" |

### review-service — `AdminReviewService`

| Trigger | Entity | Action | Details |
|---------|--------|--------|---------|
| Review flagged by admin | REVIEW | FLAG | "Review flagged by admin" |
| Review removed by admin | REVIEW | DELETE | "Review removed by admin" |
| Review published by admin | REVIEW | PUBLISH | "Review published by admin" |

---

## Adding Audit Logging to a New Operation

1. Inject `AuditService` via constructor injection.
2. Call the appropriate typed helper after the operation succeeds.
3. If no helper exists for your action, **add one to `AuditService`** rather than calling `logAction` directly.

```java
// Good
auditService.logApprove(EntityType.DRIVER, driverId, adminEmail, "Driver approved");

// Avoid — only if no typed helper exists
auditService.logAction(AuditAction.COMPLETE, EntityType.TRIP, tripId, userId, "Trip completed");
```
