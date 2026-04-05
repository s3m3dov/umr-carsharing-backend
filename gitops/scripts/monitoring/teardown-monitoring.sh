#!/bin/bash
set -e

echo "context: $(kubectl config current-context)"
echo "This will REMOVE the monitoring stack."
read -p "Continue? [y/N] " -n 1 -r; echo
[[ $REPLY =~ ^[Yy]$ ]] || exit 0

for release in fluent-bit tempo loki kube-prometheus-stack; do
    echo "Removing $release..."
    helm uninstall "$release" --namespace monitoring 2>/dev/null || echo "  (not installed)"
done

echo ""
echo "Done. To delete namespace: kubectl delete namespace monitoring"
