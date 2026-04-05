# Car Sharing Application

A carpooling platform designed for Tanzania to facilitate ride sharing between students at the university.

## Project Overview

This application provides a robust backend for a carpooling service, allowing users to offer and find rides, manage vehicles, and track ride history. It leverages a **Kubernetes-native microservices architecture** for scalability and efficiency.

---

## Repository Map

### Services

All services run on port **8080** inside the container (Spring Boot default). They are routed by service name in Kubernetes, not by port.

| Module | Path | Swagger UI |
|--------|------|------------|
| `api-gateway` | `api-gateway/` | — |
| `auth-service` | `auth-service/` | `/auth-service/swagger-ui.html` |
| `user-service` | `user-service/` | `/user-service/swagger-ui.html` |
| `trip-service` | `trip-service/` | `/trip-service/swagger-ui.html` |
| `review-service` | `review-service/` | `/review-service/swagger-ui.html` |
| `notification-service` | `notification-service/` | `/notification-service/swagger-ui.html` |
| `common` | `common/` | Shared library (no runtime) |

Each service follows the same internal layout:

```
<service>/src/main/java/com/cimhub/<service>/
  controller/     # REST controllers + ApiExamples.java (OpenAPI examples)
  service/        # Business logic
  repository/     # Spring Data repositories
  model/          # JPA / MongoDB entities
  dto/
    request/      # Inbound payloads
    response/     # Outbound payloads
  config/         # Spring config beans (Security, OpenAPI, RestClient…)
  exception/      # Service-specific error codes and handlers
```

### Shared Library (`common/`)

Shared across all services via Maven dependency:

```
common/src/main/java/com/cimhub/common/
  dto/              # ApiResponseWrapper, ApiError, ErrorBody, ErrorDetail
  enums/            # UserType, DriverStatus, NotificationType, AuditAction…
  exception/        # BusinessException, ErrorCode, GlobalExceptionHandler…
  filter/           # RequestIdFilter
  model/            # AuditLog, Points
  service/          # AuditService
  util/             # AdminGuard
  config/           # PageableConfig, PaginationConfig
```

### Infrastructure / GitOps (`gitops/`)

```
gitops/
  gitops.sh                          # Main entrypoint: setup | build | apply
  base/                              # Kustomize base (deployments, configmap, secrets, RBAC)
    deployments/                     # One YAML per service
    secrets.yaml.example             # Template — copy to secrets.yaml and fill in
  overlays/
    local/                           # Kind cluster overrides
    production/                      # GCP overrides
  monitoring/                        # Prometheus, Grafana, Loki, Tempo, Fluent Bit
    values/                          # Helm values per stack component
  scripts/
    local/                           # setup-kind.sh, build-kind.sh, create-secrets.sh
    gcp/                             # build-gcp.sh, connect-gcp.sh
    monitoring/                      # setup-monitoring.sh, teardown-monitoring.sh
    common.sh                        # Shared shell helpers
  dashboard/                         # Kubernetes Dashboard admin user
```

### Documentation (`.docs/`)

```
.docs/
  INDEX.md                           # Full documentation index (start here)
  technical/
    crucial/
      architecture.md                # System design, microservices, tech stack
      api-guide.md                   # Auth, response formats, implementation details
      api-reference.md               # Complete endpoint reference
      ...
    devops/
      gitops.md                      # GitOps workflow, scripts, commands
      ...
    openapi/                         # OpenAPI 3.0 docs
    testing/                         # Test coverage, test strategy
  product/
    user-flows.md                    # Passenger and driver journeys
    roadmap.md                       # Current state and planned features
    tasks.md                         # Development backlog
    todo.md
  domain/
    glossary.md                      # User, Driver, Passenger, Trip, Booking, Ride…
  services/                          # Service-specific documentation
  uni/                               # University-specific documentation
  samples/                           # Sample code snippets
```

### Tests

```
tests/
  rest-client/
    admin/                           # IntelliJ HTTP client files for admin flows
    user/                            # IntelliJ HTTP client files for user flows

<module>/src/test/java/               # Unit tests (JUnit 5 + Mockito)
  service/                           # Service layer tests
  controller/                        # Controller tests (future)
  repository/                        # Repository tests (future)
```

See **[`.docs/technical/testing/TESTING.md`](.docs/technical/testing/testing.md)** for comprehensive testing guide.

### Root-level Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven parent POM — all modules declared here |
| `mvnw` / `mvnw.cmd` | Maven wrapper |
| `kind-config.yaml` | Local Kind cluster node configuration |
| `CHANGELOG.md` | Version history |
| `.docs/INDEX.md` | Documentation index |

---

## Getting Started

Follow the detailed **[Operations & Setup Guide](.docs/technical/devops/gitops.md)**.

Quick steps:
1. Copy `gitops/base/secrets.yaml.example` to `gitops/base/secrets.yaml` and fill in values.
2. Set up a local Kind cluster: `./gitops/gitops.sh setup`
3. Build images: `./gitops/gitops.sh build`
4. Deploy: `./gitops/gitops.sh apply local`

---

## Key Documents

| What | Where |
|------|-------|
| Full doc index | [`.docs/INDEX.md`](.docs/INDEX.md) |
| Architecture | [`.docs/technical/crucial/architecture.md`](.docs/technical/crucial/architecture.md) |
| API reference | [`.docs/technical/crucial/api-reference.md`](.docs/technical/crucial/api-reference.md) |
| Monitoring setup | [`.docs/technical/devops/monitoring.md`](.docs/technical/devops/monitoring.md) |
| GCP deployment | [`.docs/technical/devops/gcp-deployment.md`](.docs/technical/devops/gcp-deployment.md) |
| Domain glossary | [`.docs/domain/glossary.md`](.docs/domain/glossary.md) |
| User flows | [`.docs/product/user-flows.md`](.docs/product/user-flows.md) |
| Changelog | [`CHANGELOG.md`](CHANGELOG.md) |

---

## Contributors

- Hikmat Samadov (<Samadov@students.uni-marburg.de>)
- Vipul Singh (<singhv@students.uni-marburg.de>)

## License

This project is licensed under the Apache 2.0 License.
