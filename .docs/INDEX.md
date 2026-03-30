# Documentation Index

Welcome to the Car Sharing Backend documentation.

---

## Technical Documentation

### API Reference
- **[API Reference](technical/_main/api-reference.md)**: Complete endpoint docs — auth, users, vehicles, trips
- **[OpenAPI Schemas](technical/openapi/openapi-schemas.md)**: All DTO schemas, validation rules, and field constraints
- **[Integration Examples](technical/testing/integration-examples.md)**: cURL, JavaScript, Python, and Java code examples
- **[Request & Response Payloads](technical/openapi/request-response-payloads.md)**: Full payload map by service

### Architecture & Infrastructure
- **[Architecture & Tech Stack](technical/crucial/architecture.md)**: System design, microservices, and technical capabilities
- **[API Development Guide](technical/crucial/api-guide.md)**: Authentication, response formats, and implementation details
- **[API Reference](technical/crucial/api-reference.md)**: Complete endpoint docs — auth, users, vehicles, trips
- **[BlaBlaCar Architecture Overview](technical/crucial/blablacar-architecture-overview.md)**: Deep dive into BlaBlaCar's cloud-native evolution
- **[GitOps Workflow](technical/devops/gitops.md)**: Deployment rules, scripts, and commands
- **[GCP Initial Setup](technical/devops/gcp-initial-setup.md)**: First-time cloud setup guide (GKE, Artifact Registry, Static IP)
- **[GCP Deployment](technical/devops/gcp-deployment.md)**: Cloud deployment guide
- **[Monitoring & Logging](technical/devops/monitoring.md)**: Kubernetes Dashboard, Prometheus, Grafana, Loki

### Documentation Internals
- **[Documentation Approach](technical/openapi/documentation-approach.md)**: Clean code + comprehensive docs philosophy
- **[Examples Import Approach](technical/openapi/examples-import-approach.md)**: How `ApiExamples.java` files work
- **[Examples Location Guide](technical/openapi/examples-location-guide.md)**: Where all payload examples live
- **[OpenAPI Summary](technical/openapi/openapi-summary.md)**: What was implemented in the OpenAPI pass

---

## Product & Vision

- **[Product Roadmap](product/roadmap.md)**: Current state and planned enhancements
- **[User Flows](product/user-flows.md)**: Detailed journeys for passengers and drivers
- **[Tasks & Backlog](product/tasks.md)**: Development tasks for current phases
- **[Presentations](uni/)**: University-level reports and presentations

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
| Trip Service | 8084 | `/trip-service/swagger-ui.html` |
