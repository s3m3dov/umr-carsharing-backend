# Services Documentation Index

**Purpose:** Central index for service-specific architecture and design documentation  
**Version:** 1.0  
**Last Updated:** April 2026

---

## Overview

This section covers individual service architecture, ownership boundaries, and service-specific patterns.

---

## Documentation Files

| Document | Description |
|----------|-------------|
| [user-service/user-profile-architecture.md](./user-service/user-profile-architecture.md) | Ownership boundaries for auth users and driver/passenger profile collections |
| [auth-service/auth-alignment-plan.md](./auth-service/auth-alignment-plan.md) | Authentication service alignment and refactoring plan |
| [audit/audit-logging.md](./audit/audit-logging.md) | Audit logging implementation and patterns |

---

## Quick Reference

### Available Services

| Service | Port | Path Prefix |
|---------|------|------------|
| Auth Service | 8081 | `/auth-service` |
| User Service | 8082 | `/user-service` |
| Trip Service | 8083 | `/trip-service` |
| Review Service | 8084 | `/review-service` |
| Notification Service | 8085 | `/notification-service` |

---

## Related Documentation

- [API Reference](../api-architecture/api-reference.md)
- [Architecture Overview](../api-architecture/architecture.md)
- [Refactoring Analysis](../refactoring/refactoring-analysis.md)