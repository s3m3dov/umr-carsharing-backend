#!/bin/bash

# Script to set up Kind cluster for local development
# Usage: ./setup-kind.sh

set -e

CLUSTER_NAME="carsharing-local"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../common.sh"
PROJECT_ROOT=$(get_project_root "$SCRIPT_DIR" 3)

echo "🚀 Setting up Kind cluster for local development..."

# Check required commands
require_command "kind" "https://kind.sigs.k8s.io/docs/user/quick-start/#installation"
require_command "kubectl" "https://kubernetes.io/docs/tasks/tools/"

# Check if cluster already exists
if kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
    echo "✅ Cluster '$CLUSTER_NAME' already exists"
    read -p "Do you want to delete and recreate it? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Deleting existing cluster..."
        kind delete cluster --name "$CLUSTER_NAME"
    else
        echo "Using existing cluster"
        kind export kubeconfig --name "$CLUSTER_NAME"
        kubectl cluster-info --context "kind-${CLUSTER_NAME}"
        exit 0
    fi
fi

# Create Kind cluster
echo "📦 Creating Kind cluster (this may take a few minutes)..."
kind create cluster --config "$PROJECT_ROOT/kind-config.yaml"

# Wait for cluster to be ready
echo "⏳ Waiting for cluster to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=300s

# Install NGINX Ingress Controller
echo "📦 Installing NGINX Ingress Controller..."
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

# Wait for ingress controller to be ready
echo "⏳ Waiting for ingress controller..."
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=300s

# Create namespace
echo "📦 Creating carsharing namespace..."
kubectl create namespace carsharing --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "✅ Kind cluster setup complete!"
echo ""
echo "Cluster info:"
kubectl cluster-info --context "kind-${CLUSTER_NAME}"
echo ""
echo "Next steps:"
echo "1. Create secrets: ./gitops/gitops.sh secrets"
echo "2. Build and load images: ./gitops/gitops.sh build"
echo "3. Deploy services: ./gitops/gitops.sh apply local"
echo "4. Check status: kubectl get pods -n carsharing"
echo ""
echo "To delete the cluster later:"
echo "   kind delete cluster --name $CLUSTER_NAME"
