# Kubernetes Monitoring Guide

This document covers monitoring, observability, and logging for the Kubernetes-deployed Car Sharing Backend application.

---

## Kubernetes Dashboard

[Kubernetes Dashboard](https://kubernetes.io/docs/tasks/access-kubernetes-api/web-ui-dashboard/) is a web-based UI for managing Kubernetes clusters.

### Installation

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```

### Access Dashboard

1. Create admin service account:
```bash
kubectl create serviceaccount admin -n kubernetes-dashboard
kubectl create clusterrolebinding admin-binding --clusterrole=cluster-admin --serviceaccount=kubernetes-dashboard:admin
```

2. Get token:
```bash
kubectl -n kubernetes-dashboard create token admin
```

3. Proxy access:
```bash
kubectl proxy
```

4. Open: `http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/`

### Key Metrics to Monitor

| Metric | Description | Alert Threshold |
|--------|-------------|-----------------|
| Pod CPU Usage | CPU cores used by pods | > 80% sustained |
| Pod Memory Usage | RAM used by pods | > 85% sustained |
| Pod Restarts | Number of pod restarts | > 3 in 1 hour |
| Pod Ready | Pods ready / total pods | < 100% |
| Deployment Replicas | Actual vs desired replicas | Desired - Actual > 0 |
| Service Endpoints | Ready endpoints count | < expected |

---

## Quick Commands

```bash
# View pod logs
kubectl logs -f deployment/auth-service -n carsharing

# View pod resource usage
kubectl top pods -n carsharing

# Describe pod for issues
kubectl describe pod <pod-name> -n carsharing
```

---

## GitOps Integration

For automated deployments and monitoring stack setup, see the [GitOps Workflow](gitops.md) document.

The GitOps pipeline includes scripts for:
- Deploying the monitoring stack via Helm
- Setting up Prometheus and Grafana
- Configuring AlertManager
- Deploying Loki for log aggregation

---

## Not Yet Implemented

The following monitoring components are planned but not yet implemented:

### Prometheus + Grafana

For production monitoring, use the Prometheus Operator with Grafana.

**Installation (Helm)**

```bash
# Add Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install kube-prometheus-stack
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace
```

**Key Dashboards**

1. **Kubernetes / Pods** - Pod-level CPU/memory
2. **Kubernetes / Nodes** - Node resource usage
3. **Kubernetes / Deployments** - Deployment replica status

---

### Loki Logging

[Loki](https://grafana.com/oss/loki/) is a horizontally-scalable, highly-available log aggregation system.

**Installation (Helm)**

```bash
# Add Grafana repo
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

# Install Loki Stack (Loki + Promtail + Grafana)
helm install loki grafana/loki-stack \
  --namespace monitoring \
  --create-namespace
```

**Configuration**

Promtail automatically scrapes logs from:
- `/var/log/pods/*/*.log` (Kubernetes pod logs)
- `/var/log/*.log` (system logs)

**Custom Log Labels**

```yaml
# promtail-config.yaml
clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: carsharing
    pipeline_stages:
      - regex:
          expression: '(level|lvl)=(?P<level>\w+).*msg="(?P<message>[^"]+)"'
      - labels:
          level:
          service:
    kubernetes:
      labels:
        app:
        namespace:
```

**Log Queries**

```
# All logs for a service
{app="carsharing-auth-service"}

# Error logs only
{app="carsharing-auth-service"} | json | level="error"

# Search by trace ID
{app="carsharing-trip-service"} | json | trace_id="abc123"

# Recent errors
rate({app=~"carsharing-.*"}[5m]) | json | level="error"
```

---

### Alerting

**Prometheus Alert Rules**

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: carsharing-alerts
spec:
  groups:
    - name: carsharing.rules
      rules:
        - alert: HighPodCPU
          expr: sum(rate(container_cpu_usage_seconds_total{pod=~"carsharing-.*"}[5m])) by (pod) > 0.8
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: "High CPU usage on {{ $labels.pod }}"

        - alert: HighPodMemory
          expr: sum(container_memory_working_set_bytes{pod=~"carsharing-.*"}) by (pod) / sum(container_spec_memory_limit_bytes{pod=~"carsharing-.*"}) by (pod) > 0.85
          for: 5m
          labels:
            severity: warning
          annotations:
            summary: "High memory usage on {{ $labels.pod }}"

        - alert: PodRestartingTooMuch
          expr: increase(kube_pod_container_status_restarts_total{pod=~"carsharing-.*"}[1h]) > 3
          labels:
            severity: critical
          annotations:
            summary: "Pod {{ $labels.pod }} restarting frequently"
```

**AlertManager Configuration**

```yaml
# alertmanager-config.yaml
apiVersion: monitoring.coreos.com/v1beta1
kind: AlertmanagerConfig
metadata:
  name: carsharing-alerts
spec:
  route:
    groupBy: ['alertname']
    groupWait: 10s
    groupInterval: 10s
    repeatInterval: 12h
    receiver: 'slack'
  receivers:
    - name: 'slack'
      slackConfigs:
        - channel: '#alerts'
          sendResolved: true
          apiUrl:
            secretKeyRef:
              name: alertmanager-slack
              key: api_url
```

---

### Service-Level Indicators (SLIs)

| Service | Availability | Latency (p99) | Error Rate |
|---------|--------------|---------------|------------|
| Auth Service | 99.9% | < 200ms | < 0.1% |
| User Service | 99.9% | < 300ms | < 0.1% |
| Trip Service | 99.9% | < 500ms | < 0.1% |
| API Gateway | 99.9% | < 150ms | < 0.05% |
