# Monitoring & Observability Guide

Prometheus + Grafana + Loki + Fluent Bit for the Car Sharing Backend.

---

## Stack

| Component | Role | Technology |
|-----------|------|------------|
| **Metrics** | Scraping & storage | Prometheus (kube-prometheus-stack) |
| **Dashboards** | Visualization | Grafana (bundled) |
| **Alerting** | Alert routing | AlertManager (bundled) |
| **Logs** | Aggregation | Loki (single binary) |
| **Log shipper** | DaemonSet collector | Fluent Bit (~1 MB binary, ~10 MB RAM/node) |

All deployed into the `monitoring` namespace via Helm.

---

## Usage

```bash
# Install
./gitops.sh local monitoring install
./gitops.sh gcp   monitoring install

# Grafana at http://localhost:3000  (admin / admin)
./gitops.sh local monitoring grafana

# Status
./gitops.sh local monitoring status

# Uninstall
./gitops.sh local monitoring uninstall
```

The setup script installs:
1. `kube-prometheus-stack` — Prometheus + Grafana + AlertManager + node-exporter + kube-state-metrics
2. `loki` — single binary, no caches, no gateway (`grafana/loki`)
3. `fluent-bit` — DaemonSet log shipper (`fluent/fluent-bit`)

---

## Metrics (Prometheus)

Services expose `/actuator/prometheus`, scraped via `ServiceMonitor` every 30s. Retention: 15 days.

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

Fluent Bit runs as a DaemonSet, tails container logs from `/var/log/containers/`, enriches with Kubernetes metadata, and ships to Loki. JSON fields are merged automatically via `Merge_Log`.

### LogQL

```logql
# All logs for a service
{app="user-service", namespace="carsharing"}

# Errors only
{namespace="carsharing"} | json | level="ERROR"

# By keyword
{namespace="carsharing"} |= "NullPointerException"

# Error rate
sum(rate({namespace="carsharing"} | json | level="ERROR" [5m])) by (app)
```

---

## Alerting

AlertManager is enabled. Default rules cover pod crash-looping, high CPU/memory, node-not-ready.

Custom rules via `PrometheusRule`:

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
| `gitops/monitoring/values/prometheus-stack-values.yaml` | Prometheus + Grafana + AlertManager |
| `gitops/monitoring/values/loki-values.yaml` | Loki single binary |
| `gitops/monitoring/values/fluent-bit-values.yaml` | Fluent Bit DaemonSet (log shipper) |
| `gitops/scripts/monitoring/setup-monitoring.sh` | Install script |
| `gitops/scripts/monitoring/teardown-monitoring.sh` | Uninstall script |

---

## Upgrade Path

None of these additions require replacing anything already set up:

| When | Add | Why |
|------|-----|-----|
| You need distributed tracing | **Grafana Tempo** | OTLP trace backend |
| Prometheus local retention isn't enough | **Grafana Mimir** or **Thanos** | Long-term metrics storage with S3/GCS backend |
