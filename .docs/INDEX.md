# Documentation Index

Welcome to the Car Sharing Backend documentation.

---

## Technical Documentation

### API Reference & Architecture
- **[API Architecture Index](technical/api-architecture/INDEX.md)**: Central index for API design and endpoints
- **[API Reference](technical/api-architecture/api-reference.md)**: Complete endpoint docs — auth, users, vehicles, trips, rides, bookings, reviews, notifications

### OpenAPI & Payloads
- **[OpenAPI Index](technical/openapi/INDEX.md)**: Central index for schemas, validation, and documentation
- **[OpenAPI Schemas](technical/openapi/openapi-schemas.md)**: All DTO schemas, validation rules, and field constraints
- **[Request & Response Payloads](technical/openapi/request-response-payloads.md)**: Full payload map by service

### Testing
- **[Testing Index](technical/testing/INDEX.md)**: Central index for testing guides
- **[Testing Guide](technical/testing/testing.md)**: Comprehensive testing documentation — running tests, code coverage, test types
- **[Testing Quick Reference](technical/testing/testing_quick_reference.md)**: Quick commands for running tests and viewing coverage
- **[Integration Examples](technical/testing/integration-examples.md)**: cURL, JavaScript, Python, and Java code examples

### Architecture & Infrastructure
- **[Architecture & Tech Stack](technical/api-architecture/architecture.md)**: System design, microservices, and technical capabilities
- **[API Development Guide](technical/api-architecture/api-guide.md)**: Authentication, response formats, and implementation details
- **[BlaBlaCar Architecture Overview](technical/api-architecture/blablacar-architecture-overview.md)**: Deep dive into BlaBlaCar's cloud-native evolution
- **[User & Profile Architecture](technical/services/user-service/user-profile-architecture.md)**: Ownership boundaries for auth users and driver/passenger profile collections

### Services
- **[Services Index](technical/services/INDEX.md)**: Central index for service-specific documentation

### DevOps & Deployment
- **[DevOps Index](technical/devops/INDEX.md)**: Central index for deployment and operations
- **[GitOps Workflow](technical/devops/gitops.md)**: Deployment rules, scripts, and commands
- **[GCP Initial Setup](technical/devops/gcp-initial-setup.md)**: First-time cloud setup guide (GKE, Artifact Registry, Static IP)
- **[GCP Deployment](technical/devops/gcp-deployment.md)**: Cloud deployment guide
- **[Monitoring & Logging](technical/devops/monitoring.md)**: Kubernetes Dashboard, Prometheus, Grafana, Loki

---

## Product & Vision

- **[Product Roadmap](product/roadmap.md)**: Current state and planned enhancements
- **[User Flows](product/user-flows.md)**: Detailed journeys for passengers and drivers
- **[Tasks & Backlog](product/tasks.md)**: Development tasks for current phases
- **[Presentations](uni/)**: University-level reports and presentations

### University Reports
- **[Backend Technical Report (Markdown)](uni/reports/Backend_Technical_Report_Carsharing.md)**: Backend-focused technical report (D1-D6, E1-E2)

---

## Domain Reference

- **[Glossary](domain/glossary.md)**: Definitions for User, Driver, Passenger, Trip, Booking, Ride, and more

---

## Other

- **[Changelog](../CHANGELOG.md)**: Version history and project updates

---

## Service Endpoints (Quick Reference)

All services are accessed through the API Gateway at **port 8080**. Path prefixes route to the correct service.

| Service | Path Prefix | Swagger UI |
|---------|-------------|------------|
| API Gateway | `http://localhost:8080` | `http://localhost:8080/swagger-ui.html` (aggregated) |
| Auth Service | `/auth-service` | `/auth-service/v3/api-docs` loaded via gateway dropdown |
| User Service | `/user-service` | `/user-service/v3/api-docs` loaded via gateway dropdown |
| Trip Service | `/trip-service` | `/trip-service/v3/api-docs` loaded via gateway dropdown |
| Review Service | `/review-service` | `/review-service/v3/api-docs` loaded via gateway dropdown |
| Notification Service | `/notification-service` | `/notification-service/v3/api-docs` loaded via gateway dropdown |
