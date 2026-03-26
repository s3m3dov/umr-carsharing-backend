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

Production URL: http://35.186.208.67

---

## Logging & Monitoring with Loki and Grafana

There are a few different architectural approaches to implementing Loki and Grafana for your Spring Boot microservices running on Kubernetes (Kind and GKE). Here are the primary ways you could set this up, from the most standard Kubernetes approach to application-specific methods:

### 1. The Standard K8s Approach: Helm + Promtail (Recommended)

This is the industry-standard way to deploy the LGTM (Loki, Grafana, Tempo, Mimir) stack in Kubernetes.

- **How it works:**
  - Loki is deployed as a centralized log storage system.
  - Promtail is deployed as a Kubernetes DaemonSet (one pod per node). It automatically discovers all running containers on that node, reads their stdout/stderr logs, and forwards them to Loki.
  - Grafana connects to Loki as a data source to visualize the logs.
  - Spring Boot: Your applications just need to log to the console (standard output), preferably in JSON format so Loki can parse fields (like traceId, level, etc.) automatically.
- **Pros:** Zero changes required to your application code or dependencies. Promtail captures logs even if a container crashes.
- **Cons:** Requires managing Helm charts or generating manifests from them.
- **Implementation Path:** We would write a script (e.g., `gitops/scripts/common/setup-monitoring.sh`) that uses Helm to install the `grafana/loki-stack` (which bundles Loki, Promtail, and Grafana) into a new `monitoring` namespace.

### 2. The GitOps Native Approach: Raw Manifests / Kustomize

Since you have a strong Kustomize-based GitOps structure (`gitops/base`, `gitops/overlays`), you could integrate the monitoring stack directly into it.

- **How it works:** Instead of running Helm commands imperatively, we extract the generated YAML manifests for Loki, Promtail, and Grafana, and place them in `gitops/base/monitoring/`.
- **Pros:** Perfectly aligns with your existing `./gitops.sh` local deploy workflow. Everything is declarative and stored in version control.
- **Cons:** Loki and Grafana manifests can be extremely complex and long, making them hard to maintain manually compared to using Helm.
- **Implementation Path:** Use `helm template` to generate the raw YAML files, save them into your `gitops/base` directory, and add them to your `kustomization.yaml`.

### 3. The Direct Push Approach: Loki Logback Appender

Instead of using an agent like Promtail to scrape logs from the outside, you can configure your Spring Boot applications to push logs directly to Loki.

- **How it works:** You add a dependency like `loki-logback-appender` to your common module's `pom.xml`. Then, you configure `logback-spring.xml` in each service to send log events directly to the Loki HTTP endpoint (e.g., `http://loki:3100/loki/api/v1/push`).
- **Pros:** No need to run Promtail as a DaemonSet. You get very fine-grained control over exactly what gets sent to Loki directly from the application layer.
- **Cons:** If Loki is temporarily down, the application might block or drop logs (depending on the appender configuration). If the JVM crashes completely (e.g., OOM), you might lose the final error logs that a container-level scraper would catch.
- **Implementation Path:** Update the `pom.xml` files, add a `logback-spring.xml` to `common/src/main/resources`, and deploy Loki and Grafana (via Helm or Kustomize).

### 4. Hybrid Environment Approach (Local vs. GCP)

Since you are deploying to both local Kind and GCP (GKE Autopilot):

- **Local (Kind):** Run the full Loki + Promtail + Grafana stack to simulate production.
- **GCP (GKE):** GKE Autopilot automatically ships container logs to Google Cloud Logging (formerly Stackdriver).
  - **Option A:** Use GCP Cloud Logging instead of Loki for production, and configure your GCP Grafana instance to use Cloud Logging as a data source.
  - **Option B:** Disable GCP Logging and run Promtail + Loki in GKE to keep the local and production stacks identical.

### Recommendation

I highly recommend Approach 1 (Helm + Promtail) combined with updating your Spring Boot apps to output JSON logs.

We could structure it like this:
1. Update the common module to configure JSON console logging for all Spring Boot services (makes querying in Grafana much easier).
2. Add a `setup-monitoring.sh` script to your GitOps tool that uses Helm to deploy the stack to both local and gcp clusters.
3. Add a dashboard or monitoring command to `./gitops.sh` to easily port-forward to Grafana.
