# Car Sharing Backend Architecture

## Overview

This document describes the architecture of the car sharing backend microservices system.

## Services

| Service | Port | Description |
|---------|------|-------------|
| api-gateway | 8080 | API Gateway with JWT validation and routing |
| user-service | 8081 | User, Driver, Passenger, Vehicle management |
| auth-service | 8082 | Authentication and authorization |
| trip-service | 8083 | Trip and Booking management |
| review-service | 8084 | Review management |
| notification-service | 8085 | Notifications |

## API Gateway

### Routing Configuration

All services are routed through the API Gateway with path-based routing:

- `/user-service/**` → user-service:8080
- `/auth-service/**` → auth-service:8080
- `/trip-service/**` → trip-service:8080
- `/review-service/**` → review-service:8080
- `/notification-service/**` → notification-service:8080

### Security

#### Public Paths
These paths bypass JWT validation:
- `/auth-service/api/auth/login`
- `/auth-service/api/auth/signup`
- `/auth-service/api/auth/validate`
- `/auth-service/api/auth/reset-password`
- `/auth-service/api/auth/reset-password/confirm`
- `/webjars/**`
- Swagger/OpenAPI docs

#### Admin Paths
Admin paths require ADMIN role:
- `/**/admin/**`
- `/**/api/**/admin/**`

### Filters

1. **JwtValidationFilter** (Order: Highest)
   - Validates JWT tokens with auth-service
   - Strips incoming X-User-* headers to prevent spoofing
   - Attaches user info (email, id, role) as headers

2. **AdminAuthorizationFilter** (Order: Highest + 1)
   - Validates ADMIN role for admin paths
   - Returns 403 Forbidden for unauthorized access

## Admin Panel

### Authentication
- All admin endpoints require JWT token with ADMIN role
- Gateway-level RBAC enforcement
- X-User-Role header injected after token validation

### Endpoints

#### User Service
- `GET /api/drivers` - List all drivers
- `GET /api/drivers/{id}` - Get driver detail
- `POST /api/drivers/{id}/approve` - Approve driver
- `POST /api/drivers/{id}/reject` - Reject driver
- `POST /api/drivers/bulk-approve` - Bulk approve
- `POST /api/drivers/bulk-reject` - Bulk reject
- `PUT /api/drivers/{id}` - Update driver
- `DELETE /api/drivers/{id}` - Delete driver
- `GET /api/drivers/{id}/documents` - Get driver documents
- `GET /api/passengers` - List passengers
- `GET /api/passengers/{id}` - Get passenger detail
- `POST /api/passengers` - Create passenger
- `PUT /api/passengers/{id}` - Update passenger
- `DELETE /api/passengers/{id}` - Delete passenger
- `GET /api/vehicles/admin` - List vehicles
- `GET /api/vehicles/admin/{id}` - Get vehicle detail
- `POST /api/vehicles/admin` - Create vehicle
- `PUT /api/vehicles/admin/{id}` - Update vehicle
- `DELETE /api/vehicles/admin/{id}` - Delete vehicle

#### Trip Service
- `GET /api/trips/admin` - List trips
- `GET /api/trips/admin/{id}` - Get trip detail
- `GET /api/trips/admin/upcoming` - Get upcoming trips
- `GET /api/trips/admin/history` - Get trip history
- `DELETE /api/trips/admin/{id}` - Cancel trip
- `GET /api/bookings/admin` - List bookings
- `GET /api/bookings/admin/{id}` - Get booking detail
- `PUT /api/bookings/admin/{id}` - Update booking
- `DELETE /api/bookings/admin/{id}` - Cancel booking
- `GET /api/bookings/admin/passenger/{passengerId}` - Get passenger bookings
- `GET /api/reports/drivers` - Driver performance report
- `GET /api/reports/bookings` - Bookings summary
- `GET /api/reports/revenue` - Revenue report
- `GET /api/reports/passengers` - Passenger activity

#### Review Service
- `GET /api/reviews/admin` - List reviews
- `GET /api/reviews/admin/{id}` - Get review detail
- `POST /api/reviews/admin/{id}/flag` - Flag review
- `DELETE /api/reviews/admin/{id}` - Remove review
- `POST /api/reviews/admin/{id}/publish` - Publish review

### Audit Logging

All admin actions are logged to MongoDB collection `audit_logs`:

```json
{
  "id": "string",
  "action": "APPROVE|REJECT|UPDATE|DELETE|CREATE|CANCEL|FLAG|REMOVE|PUBLISH",
  "entityType": "DRIVER|PASSENGER|VEHICLE|TRIP|BOOKING|REVIEW",
  "entityId": "string",
  "performedBy": "admin@email.com",
  "details": "Action details",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Pagination

Default pagination settings (configurable per service):

```yaml
pagination:
  default-page-size: 20
  max-page-size: 100
  min-page-size: 1
```

## Swagger/OpenAPI

Each service exposes OpenAPI documentation at:
- `/v3/api-docs` - OpenAPI JSON
- `/swagger-ui.html` - Swagger UI

Gateway aggregates all services at `/swagger-ui.html`.

## Exception Handling

- **ForbiddenException** - Returned for unauthorized access (403)
- **ResourceNotFoundException** - Returned for missing resources (404)
- **BusinessException** - Base exception with error codes
- **GlobalExceptionHandler** - Centralized exception handling in common module
