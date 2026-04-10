# API Architecture Documentation Index

**Purpose:** Central index for API architecture, design patterns, and reference documentation  
**Version:** 1.0  
**Last Updated:** April 2026

---

## Overview

This section covers API design principles, architectural patterns, endpoint documentation, and development guidelines.

---

## Documentation Files

| Document | Description |
|----------|-------------|
| [api-reference.md](./api-reference.md) | Complete endpoint documentation — auth, users, vehicles, trips, rides, bookings, reviews, notifications |
| [architecture.md](./architecture.md) | System design, microservices architecture, and technical capabilities |
| [api-guide.md](./api-guide.md) | Authentication, response formats, and implementation details |
| [blablacar-architecture-overview.md](./blablacar-architecture-overview.md) | Deep dive into BlaBlaCar's cloud-native evolution |

---

## Quick Reference

### Architecture Patterns

| Pattern | Description |
|---------|-------------|
| API Gateway | Single entry point routing to microservices |
| Service-to-Service | Direct communication via Feign clients |
| Event-Driven | Async notifications via RabbitMQ |

### Authentication

- JWT-based stateless authentication
- Token validation at API Gateway level
- Service-specific role-based access

---

## Related Documentation

- [OpenAPI Schemas](../openapi/openapi-schemas.md)
- [Testing Guide](../testing/testing.md)
- [Refactoring Analysis](../refactoring/refactoring-analysis.md)