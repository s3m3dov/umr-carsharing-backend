# Monitoring & Observability Guide

Prometheus + Grafana + Loki + Fluent Bit + Tempo for the Car Sharing Backend.

---

## Stack

| Component | Role | Technology |
|-----------|------|------------|
| **Metrics** | Scraping & storage | Prometheus (kube-prometheus-stack) |
| **Dashboards** | Visualization | Grafana (bundled) |
| **Alerting** | Alert routing | AlertManager — **disabled** (no users) |
| **Logs** | Aggregation | Loki (single binary) |
| **Log shipper** | DaemonSet collector | Fluent Bit |
| **Tracing** | Distributed traces | Tempo (single binary, OTLP) |

All deployed into the `monitoring` namespace via Helm.

---

## Usage

```bash
# Install
./gitops.sh local monitoring install
./gitops.sh gcp   monitoring install

# Status
./gitops.sh local monitoring status

# Uninstall
./gitops.sh local monitoring uninstall
```

## Accessing Grafana

Grafana is routed through the API Gateway at the `/grafana` subpath — no separate ingress or port-forward needed.

| Environment | URL | Credentials |
|-------------|-----|-------------|
| Local | http://localhost:8888/grafana | admin / admin |
| GCP | http://35.228.204.85/grafana | admin / admin |

Swagger UI is available on the same domain:

| Environment | URL |
|-------------|-----|
| Local | http://localhost:8888/swagger-ui.html |
| GCP | http://35.228.204.85/swagger-ui.html |

### How it works

The API Gateway (Spring Cloud Gateway) proxies `/grafana/**` to `kube-prometheus-stack-grafana.monitoring.svc.cluster.local:80`. Grafana is configured with `serve_from_sub_path: true` and `root_url` set to the `/grafana/` prefix, so all assets and redirects resolve correctly.

The `/grafana/**` path is on the public path list in the gateway — no JWT token required to access it.

The setup script installs:
1. `kube-prometheus-stack` — Prometheus + Grafana + node-exporter + kube-state-metrics
2. `loki` — single binary, no caches, no gateway (`grafana/loki`)
3. `fluent-bit` — DaemonSet log shipper (`fluent/fluent-bit`)
4. `tempo` — single binary trace backend (`grafana/tempo`)

---

## Resource Budgets

Sized for zero-user / development use. Scale up when real traffic arrives.

### Application Services

| Service | Memory request | Memory limit | CPU request | CPU limit |
|---------|---------------|--------------|-------------|-----------|
| api-gateway | 128Mi | 256Mi | 50m | 200m |
| auth-service | 128Mi | 256Mi | 50m | 200m |
| user-service | 128Mi | 256Mi | 50m | 200m |
| trip-service | 128Mi | 256Mi | 50m | 200m |
| review-service | 128Mi | 256Mi | 50m | 200m |
| notification-service | 128Mi | 256Mi | 50m | 200m |

> HPAs are not configured — min=max=1 adds overhead with no benefit at this scale.

### Monitoring Stack

| Component | Memory request | Memory limit | CPU request | CPU limit | Storage |
|-----------|---------------|--------------|-------------|-----------|---------|
| Prometheus | 128Mi | 256Mi | 50m | 200m | — |
| Grafana | 64Mi | 128Mi | 25m | 100m | — |
| Loki | 64Mi | 128Mi | 25m | 100m | 500Mi |
| Tempo | 64Mi | 128Mi | 25m | 100m | 500Mi |
| Fluent Bit (per node) | 16Mi | 32Mi | 10m | 50m | — |
| AlertManager | — | — | — | — | disabled |

---

## Metrics (Prometheus)

Services expose `/actuator/prometheus`, scraped via `ServiceMonitor` every 30s. Retention: 7 days.

### PromQL

```promql
# Request rate per service
sum(rate(http_server_requests_seconds_count{application=~".+"}[5m])) by (application)

# p99 latency
histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (application, le))

# 5xx error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)
/ sum(rate(http_server_requests_seconds_count[5m])) by (application)

# JVM heap usage
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
```

---

## Logs (Loki + Fluent Bit)

Fluent Bit runs as a DaemonSet, tails `/var/log/containers/`, enriches with Kubernetes metadata, and ships to Loki. JSON fields are merged into the log line automatically via `Merge_Log`.

Loki labels: `job`, `namespace`, `pod`, `container`.

### LogQL

```logql
# All logs for a service
{namespace="carsharing", pod=~"user-service.*"}

# Errors only
{namespace="carsharing"} | json | level="ERROR"

# By keyword
{namespace="carsharing"} |= "NullPointerException"

# Error rate
sum(rate({namespace="carsharing"} | json | level="ERROR" [5m])) by (pod)
```

---

## Tracing (Tempo)

Services send OTLP traces to `tempo.monitoring.svc.cluster.local:4318`. 100% sampling (dev).
Trace-to-log correlation is configured in Grafana via the Tempo datasource linked to Loki.

---

## Alerting

AlertManager is **disabled**. Re-enable in `prometheus-stack-values.yaml` when you have users.

Custom rules via `PrometheusRule` for future reference:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: carsharing-alerts
  namespace: monitoring
  labels:
    release: kube-prometheus-stack
spec:
  groups:
    - name: carsharing.sli
      rules:
        - alert: HighErrorRate
          expr: |
            sum(rate(http_server_requests_seconds_count{status=~"5..",application=~".+"}[5m])) by (application)
            / sum(rate(http_server_requests_seconds_count{application=~".+"}[5m])) by (application)
            > 0.01
          for: 2m
          labels:
            severity: warning
          annotations:
            summary: "{{ $labels.application }} error rate > 1%"

        - alert: HighP99Latency
          expr: |
            histogram_quantile(0.99,
              sum(rate(http_server_requests_seconds_bucket{application=~".+"}[5m])) by (application, le)
            ) > 1.0
          for: 5m
          labels:
            severity: warning

        - alert: PodRestartingTooMuch
          expr: increase(kube_pod_container_status_restarts_total{namespace="carsharing"}[1h]) > 3
          labels:
            severity: critical
```

---

## Configuration Files

| File | Purpose |
|------|---------|
| `gitops/monitoring/namespace.yaml` | `monitoring` namespace |
| `gitops/monitoring/servicemonitor.yaml` | Prometheus scrape config for carsharing services |
| `gitops/monitoring/values/prometheus-stack-values.yaml` | Prometheus + Grafana (subpath config) |
| `gitops/monitoring/values/loki-values.yaml` | Loki single binary |
| `gitops/monitoring/values/tempo-values.yaml` | Tempo single binary |
| `gitops/monitoring/values/fluent-bit-values.yaml` | Fluent Bit DaemonSet (log shipper) |
| `gitops/scripts/monitoring/setup-monitoring.sh` | Install script |
| `gitops/scripts/monitoring/teardown-monitoring.sh` | Uninstall script |
| `api-gateway/src/main/resources/application.yml` | Gateway route for `/grafana/**` + public path entry |

---

## Upgrade Path

| When | Add | Why |
|------|-----|-----|
| You have real users | Re-enable AlertManager | On-call alerting |
| Prometheus retention isn't enough | **Grafana Mimir** or **Thanos** | Long-term metrics storage with S3/GCS |
| Loki storage fills up | Increase PVC or switch to object storage | More log retention |
