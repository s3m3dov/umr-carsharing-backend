# Documentation Index

Welcome to the Car Sharing Backend documentation.

---

## Technical Documentation

### API Reference
- **[API Reference](technical/api-reference.md)**: Complete endpoint docs — auth, users, vehicles, trips, rides
- **[OpenAPI Schemas](technical/openapi-schemas.md)**: All DTO schemas, validation rules, and field constraints
- **[Integration Examples](technical/integration-examples.md)**: cURL, JavaScript, Python, and Java code examples
- **[Request & Response Payloads](technical/request-response-payloads.md)**: Full payload map by service

### Architecture & Infrastructure
- **[Microservice Organization Strategy](technical/microservice-organization-strategy.md)**: Phased evolution plan for service architecture
- **[BlaBlaCar Architecture Overview](technical/blablacar-architecture-overview.md)**: Deep dive into BlaBlaCar's cloud-native evolution
- **[Architecture & Tech Stack](technical/architecture.md)**: System design, microservices, and technical capabilities
- **[API Development Guide](technical/api-guide.md)**: Authentication, response formats, and implementation details
- **[Operations & Setup](technical/operations.md)**: Environment variables and cluster setup (Kind)
- **[GitOps Workflow](technical/gitops.md)**: Deployment rules, scripts, and commands
- **[GCP Deployment](technical/gcp-deployment.md)**: Cloud deployment guide

### Documentation Internals
- **[Documentation Approach](technical/documentation-approach.md)**: Clean code + comprehensive docs philosophy
- **[Examples Import Approach](technical/examples-import-approach.md)**: How `ApiExamples.java` files work
- **[Examples Location Guide](technical/examples-location-guide.md)**: Where all payload examples live
- **[OpenAPI Summary](technical/openapi-summary.md)**: What was implemented in the OpenAPI pass

---

## Product & Vision

- **[Product Roadmap](product/roadmap.md)**: Current state and planned enhancements
- **[User Flows](product/user-flows.md)**: Detailed journeys for passengers and drivers
- **[Tasks & Backlog](product/tasks.md)**: Development tasks for current phases
- **[Presentations](product/presentations/)**: Project slides and implementation details

---

## Domain Reference

- **[Glossary](domain/glossary.md)**: Definitions for User, Driver, Passenger, Trip, Booking, Ride, and more

---

## Other

- **[Changelog](../CHANGELOG.md)**: Version history and project updates

---

## Service Endpoints (Quick Reference)

| Service | Port | Swagger UI |
|---------|------|------------|
| API Gateway | 8080 | — |
| Auth Service | 8081 | `/auth-service/swagger-ui.html` |
| User Service | 8082 | `/user-service/swagger-ui.html` |
| Vehicle Service | 8083 | `/vehicle-service/swagger-ui.html` |
| Trip Service | 8084 | `/trip-service/swagger-ui.html` |
| Ride Service | 8085 | `/ride-service/swagger-ui.html` |
