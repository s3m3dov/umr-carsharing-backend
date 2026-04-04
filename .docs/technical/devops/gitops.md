# Manual GitOps Workflow

This project follows a "Manual GitOps" pattern. Git is the single source of truth for the desired state of the cluster.

## Rules

1. **Never** use `kubectl edit`.
2. **Never** use `kubectl patch` or `kubectl delete` for ad-hoc changes.
3. **Always** commit changes to the `gitops/` directory first.
4. **Always** use `./gitops.sh` to apply changes.
5. The first argument (`local` or `gcp`) automatically switches kubectl context.

## Quick Reference

| Command | Local | GCP |
|---------|-------|-----|
| Setup | `./gitops.sh local setup` | N/A (cluster pre-provisioned) |
| Secrets | `./gitops.sh local secrets` | `./gitops.sh gcp secrets` |
| Build | `./gitops.sh local build` | `./gitops.sh gcp build [services]` |
| Deploy | `./gitops.sh local deploy` | `./gitops.sh gcp deploy [services]` |
| Status | `./gitops.sh local status` | `./gitops.sh gcp status` |
| Diff | `./gitops.sh local diff` | `./gitops.sh gcp diff` |
| Restart | `./gitops.sh local restart [service]` | `./gitops.sh gcp restart [service]` |
| Dashboard | `./gitops.sh local dashboard` | `./gitops.sh gcp dashboard` |
| Ingress | `./gitops.sh local ingress` (port 8888) | N/A (uses external LB) |
| Monitoring | `./gitops.sh local monitoring <sub>` | `./gitops.sh gcp monitoring <sub>` |

Monitoring subcommands: `install`, `uninstall`, `status` (grafana now via http://localhost:8888/grafana)

---

## Local Development Commands

### Setup Cluster

```bash
./gitops.sh local setup
```

Creates a local Kubernetes cluster using Kind, installs NGINX ingress controller, and configures port mappings.

### Create Secrets

```bash
./gitops.sh local secrets
```

Reads variables from `.env` file and creates Kubernetes Secret in the local cluster.

### Build Images

```bash
./gitops.sh local build
```

Builds Docker images for all services and loads them into the Kind cluster.

### Deploy Changes

```bash
./gitops.sh local deploy
```

Applies Kubernetes manifests from `gitops/overlays/local` and restarts all deployments.

### Preview Changes

```bash
./gitops.sh local diff
```

Shows differences between current cluster state and manifests (dry-run).

### Check Status

```bash
./gitops.sh local status
```

Lists all deployments in the `carsharing` namespace.

### Restart Deployments

```bash
# Restart all services
./gitops.sh local restart

# Restart specific service
./gitops.sh local restart trip-service
```

### Access Application & Grafana

```bash
./gitops.sh local ingress
```

Starts port-forwarding to ingress controller (localhost:8888). Access:
- **Swagger UI:** http://localhost:8888/swagger-ui.html
- **Grafana:** http://localhost:8888/grafana (requires monitoring stack installed)

### Dashboard

```bash
./gitops.sh local dashboard
```

Deploys Kubernetes Dashboard. Access via:
1. Run: `kubectl proxy`
2. Visit: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
3. Get token: `kubectl -n kubernetes-dashboard create token admin-user`

### Monitoring

```bash
# Install Prometheus + Grafana + Loki + Fluent Bit + Tempo
./gitops.sh local monitoring install

# Check stack status
./gitops.sh local monitoring status

# Remove the stack
./gitops.sh local monitoring uninstall
```

Once installed, access Grafana at **http://localhost:8888/grafana** (via the main ingress, same port as Swagger).

Stack includes:
- **Prometheus** — metrics collection (7-day retention, 128Mi/256Mi resources)
- **Grafana** — dashboards with pre-loaded CarSharing Services dashboard (64Mi/128Mi resources)
- **Loki** — log aggregation (500Mi storage, 64Mi/128Mi resources)
- **Fluent Bit** — DaemonSet log shipper (16Mi/32Mi per node)
- **Tempo** — distributed tracing OTLP backend (500Mi storage, 64Mi/128Mi resources)

See [Monitoring & Observability Guide](monitoring.md) for resource budgets, PromQL/LogQL queries, and configuration details.

---

## GCP Production Commands

### Build & Push Images

```bash
# Build all services
./gitops.sh gcp build

# Build specific services
./gitops.sh gcp auth-service user-service trip-service
```

Builds Docker images and pushes to Google Container Registry (GCR).

### Create Secrets

```bash
./gitops.sh gcp secrets
```

Creates Kubernetes Secret in GKE cluster from `.env` file.

### Deploy

```bash
# Deploy all services
./gitops.sh gcp deploy

# Deploy specific services
./gitops.sh gcp deploy trip-service
```

Applies Kubernetes manifests from `gitops/overlays/production`.

### Check Status

```bash
./gitops.sh gcp status
```

### Preview Changes

```bash
./gitops.sh gcp diff
```

### Restart

```bash
# Restart all services
./gitops.sh gcp restart

# Restart specific service
./gitops.sh gcp restart trip-service
```

### Dashboard

```bash
./gitops.sh gcp dashboard
```

### Monitoring

```bash
# Install Prometheus + Grafana + Loki + Fluent Bit on GKE
./gitops.sh gcp monitoring install

# Port-forward Grafana to localhost:3000
./gitops.sh gcp monitoring grafana

# Check stack status
./gitops.sh gcp monitoring status

# Remove the stack
./gitops.sh gcp monitoring uninstall
```

See [Monitoring & Observability Guide](monitoring.md) for full details.

---

## Complete Workflows

### Local Development - First Time

```bash
./gitops.sh local setup                   # Create Kind cluster
./gitops.sh local secrets                 # Load .env variables
./gitops.sh local build                   # Build Docker images
./gitops.sh local deploy                  # Deploy services
./gitops.sh local monitoring install      # Install monitoring stack (optional)
./gitops.sh local ingress                 # Access app at http://localhost:8888
```

### Local Development - After Changes

```bash
./gitops.sh local build
./gitops.sh local deploy
```

### GCP Production - First Deploy

```bash
./gitops.sh gcp build                     # Build & push images to GCR
./gitops.sh gcp secrets                   # Create secrets in GKE
./gitops.sh gcp deploy                    # Deploy services
./gitops.sh gcp monitoring install        # Install monitoring stack (optional)
```

Production URL: http://35.186.208.67

---

## Monitoring

The monitoring stack (Prometheus, Grafana, Loki, Fluent Bit) is managed via `./gitops.sh monitoring` commands. See the [Monitoring & Observability Guide](monitoring.md) for the full reference including PromQL queries, LogQL examples, and alerting.
