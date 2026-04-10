# Refactoring Documentation Index

**Purpose：** Central index for all refactoring and code quality documentation  
**Version：** 1.0  
**Last Updated：** April 2026

---

## Overview

This section documents the service architecture analysis, code quality issues, refactoring recommendations, and coding standards for all microservices in the carsharing-backend project.

---

## Documentation Files

| Document | Description | Status |
|----------|-------------|--------|
| [refactoring-analysis.md](./refactoring-analysis.md) | Comprehensive technical analysis with 24 issues (8 HIGH, 10 MEDIUM, 6 LOW severity) | Current |
| [code-style-guide.md](./code-style-guide.md) | Code style guidelines, naming conventions, class structure | Current |
| [api-dto-standardization.md](./api-dto-standardization.md) | API examples and DTO structure standardization | Current |
| [folder-structure-standard.md](./folder-structure-standard.md) | Standard package organization across services | Current |

---

## Quick Summary

### Issues by Severity

| Severity | Count | Key Services Affected |
|----------|-------|-------------------|
| **HIGH** | 8 | auth-service, user-service, trip-service, review-service |
| **MEDIUM** | 10 | All services |
| **LOW** | 6 | api-gateway, trip-service |

### Critical Fixes Needed

1. **Auth-Service**： Split AuthService into RegistrationService, AuthenticationService, PasswordManagementService
2. **User-Service**： Create generic AbstractUserProfileService to eliminate duplicate code
3. **User-Service**： Fix VehicleService null return → throw exception
4. **Trip-Service**： Split TripGeospatialService (400+ lines)
5. **Trip-Service**： Replace in-memory filtering with MongoDB queries
6. **Trip-Service**: Fix N+1 query in ActiveRidesService
7. **Review-Service**: Fix TripServiceClient silent failure → throw exception
8. **Trip-Service**: Fix duplicate Trip fetch in CompletionService

---

## Folder Structure Issues

### Current Problems

| Problem | Service | Fix |
|---------|---------|-----|
| `security/` package instead of `config/` | auth-service | Move to config/ |
| `*ApiExamples.java` in controller/ | All | Move to dto/examples/ as *ApiDoc.java |
| `service/routes/` sub-package | trip-service | Move to service/helper/ |
| OSRM DTOs in dto/response/ | trip-service | Move to dto/osrm/ |
| Vehicle as nested sub-package | user-service | Evaluate extraction |

---

## Coding Standards Reference

### Package Conventions

```
src/main/java/com/cimhub/{service}/
├── {Service}Application.java     # Entry point
├── config/                    # ALL @Configuration beans
├── controller/                # REST endpoints
├── dto/
│   ├── request/              # Request DTOs
│   ├── response/            # Response DTOs
│   ├── examples/            # OpenAPI documentation (static)
│   └── osrm/              # External API DTOs
├── exception/               # Exception handlers + custom exceptions
├── model/                  # Domain entities
├── repository/             # Data access
└── service/               # Business logic
    └── helper/            # Helper utilities
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Service interface | `{Domain}Service` | `RegistrationService` |
| Service implementation | `{Domain}ServiceImpl` | `RegistrationServiceImpl` |
| Controller | `{Domain}Controller` | `TripController` |
| Request DTO | `{Action}{Entity}Request` | `CreateTripRequest` |
| Response DTO | `{Entity}Response` | `TripResponse` |
| API Examples | `{Domain}ApiDoc` | `TripApiDoc` |
| Exception | `{Concept}Exception` | `TripNotFoundException` |

---

## Related Documentation

### Technical Documents

- [Architecture Overview](../api-architecture/blablacar-architecture-overview.md)
- [User Profile Architecture](../services/user-service/user-profile-architecture.md)
- [Audit Logging](../services/audit/audit-logging.md)

### API Documents

- [API Reference](../api-architecture/api-reference.md)
- [API Guide](../api-architecture/api-guide.md)

### DevOps Documents

- [Deployment Config](../devops/)
- [Kubernetes Config](../../gitops/)

---

*For questions or updates, refer to the main codebase or contact the team.*