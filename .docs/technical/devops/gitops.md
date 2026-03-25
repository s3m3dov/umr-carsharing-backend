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
| Ingress | `./gitops.sh local ingress` | N/A (uses external LB) |

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

### Access Application

```bash
./gitops.sh local ingress
```

Starts port-forwarding to ingress controller. Access at: http://localhost:8888/swagger-ui.html

### Dashboard

```bash
./gitops.sh local dashboard
```

Deploys Kubernetes Dashboard. Access via:
1. Run: `kubectl proxy`
2. Visit: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
3. Get token: `kubectl -n kubernetes-dashboard create token admin-user`

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

---

## Complete Workflows

### Local Development - First Time

```bash
./gitops.sh local setup
./gitops.sh local secrets
./gitops.sh local build
./gitops.sh local deploy
./gitops.sh local ingress
```

### Local Development - After Changes

```bash
./gitops.sh local build
./gitops.sh local deploy
```

### GCP Production - Deploy

```bash
./gitops.sh gcp build
./gitops.sh gcp secrets
./gitops.sh gcp deploy
```

Production URL: http://34.160.91.182
