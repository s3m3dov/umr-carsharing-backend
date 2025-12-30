# Documentation

This folder contains project documentation and reference files.

## Contents

- **Car sharing .PPtx** / **Car sharing Backend.pptx** - Project presentations demonstrating the car sharing platform
- **firebase-sdk-sample.json** - Sample Firebase SDK configuration template (for reference only, not used in production)

## API Documentation

The project now uses **dynamic OpenAPI/Swagger documentation** generated from code annotations.

### Access Points

**Aggregated Documentation (All Services):**

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Select different services from the dropdown menu

**Individual Service Documentation:**

- User Service: `http://localhost:8081/swagger-ui.html`
- Vehicle Service: `http://localhost:8082/swagger-ui.html`
- Trip Service: `http://localhost:8083/swagger-ui.html`
- Ride Service: `http://localhost:8084/swagger-ui.html`
- Notification Service: `http://localhost:8085/swagger-ui.html`

**OpenAPI JSON Specs:**

- Individual services: `http://localhost:808X/v3/api-docs`
- Via Gateway: `http://localhost:8080/v3/api-docs?group=<service-name>`

The documentation is automatically generated from controller annotations and stays in sync with code changes.
