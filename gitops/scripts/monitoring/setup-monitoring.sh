#!/bin/bash
################################################################################
# Monitoring: kube-prometheus-stack + Loki + Fluent Bit + Tempo
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GITOPS_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"
MONITORING_DIR="$GITOPS_DIR/monitoring"
VALUES_DIR="$MONITORING_DIR/values"

run() { if ${DRY_RUN:-false}; then echo "[DRY-RUN] $*"; else "$@"; fi; }
[[ "${1:-}" == "--dry-run" ]] && DRY_RUN=true

command -v helm &>/dev/null || { echo "helm not installed"; exit 1; }
command -v kubectl &>/dev/null || { echo "kubectl not installed"; exit 1; }

echo "context: $(kubectl config current-context)"
read -p "Continue? [y/N] " -n 1 -r; echo
[[ $REPLY =~ ^[Yy]$ ]] || exit 0

run helm repo add prometheus-community https://prometheus-community.github.io/helm-charts 2>/dev/null || true
run helm repo add grafana https://grafana.github.io/helm-charts 2>/dev/null || true
run helm repo add fluent https://fluent.github.io/helm-charts 2>/dev/null || true
run helm repo update

run kubectl apply -f "$MONITORING_DIR/namespace.yaml"

echo ""; echo "Installing kube-prometheus-stack..."
run helm upgrade --install kube-prometheus-stack \
    prometheus-community/kube-prometheus-stack \
    --namespace monitoring \
    --values "$VALUES_DIR/prometheus-stack-values.yaml" \
    --wait --timeout 5m

echo ""; echo "Installing Loki..."
run helm upgrade --install loki grafana/loki \
    --namespace monitoring \
    --values "$VALUES_DIR/loki-values.yaml" \
    --wait --timeout 3m

echo ""; echo "Installing Fluent Bit..."
run helm upgrade --install fluent-bit fluent/fluent-bit \
    --namespace monitoring \
    --values "$VALUES_DIR/fluent-bit-values.yaml" \
    --wait --timeout 2m

echo ""; echo "Installing Tempo..."
run helm upgrade --install tempo grafana/tempo \
    --namespace monitoring \
    --values "$VALUES_DIR/tempo-values.yaml" \
    --wait --timeout 3m

echo ""; echo "Applying ServiceMonitor..."
run kubectl apply -f "$MONITORING_DIR/servicemonitor.yaml"

echo ""
echo "Done. Grafana: http://grafana.monitoring.local  (add to /etc/hosts if local)"
echo "       Or:    kubectl port-forward -n monitoring svc/kube-prometheus-stack-grafana 3000:80"
