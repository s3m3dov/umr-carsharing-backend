# OpenAPI Documentation Index

**Purpose:** Central index for OpenAPI schemas, documentation approach, and payload examples  
**Version:** 1.0  
**Last Updated:** April 2026

---

## Overview

This section covers OpenAPI specifications, DTO schemas, validation rules, and payload documentation.

---

## Documentation Files

| Document | Description |
|----------|-------------|
| [openapi-schemas.md](./openapi-schemas.md) | All DTO schemas, validation rules, and field constraints |
| [request-response-payloads.md](./request-response-payloads.md) | Full payload map by service |
| [documentation-approach.md](./documentation-approach.md) | Clean code + comprehensive docs philosophy |
| [examples-import-approach.md](./examples-import-approach.md) | How `ApiExamples.java` files work |
| [examples-location-guide.md](./examples-location-guide.md) | Where all payload examples live |
| [openapi-summary.md](./openapi-summary.md) | What was implemented in the OpenAPI pass |

---

## Quick Reference

### DTO Naming Convention

| Type | Convention | Example |
|------|------------|---------|
| Request | `{Action}{Entity}Request` | `CreateTripRequest` |
| Response | `{Entity}Response` | `TripResponse` |
| Examples | `{Domain}ApiDoc` | `TripApiDoc` |

---

## Related Documentation

- [API Reference](../api-architecture/api-reference.md)
- [Testing Integration Examples](../testing/integration-examples.md)
- [Refactoring Analysis](../refactoring/api-dto-standardization.md)