#!/bin/bash

# Script to configure kubectl for existing GKE Autopilot cluster
# Usage: ./setup-gcp.sh

set -e

# Get the directory of this script and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Function to extract value from ConfigMap YAML
get_configmap_value() {
    local key=$1
    local configmap_file="$PROJECT_ROOT/gitops/base/configmap.yaml"
    
    if [ -f "$configmap_file" ]; then
        # Extract value using grep and sed, removing quotes
        grep "^  $key:" "$configmap_file" | sed 's/.*: "\(.*\)"/\1/' | tr -d '"'
    fi
}

# Read configuration from ConfigMap or environment variables
CLUSTER_NAME="${GKE_CLUSTER_NAME:-$(get_configmap_value "GKE_CLUSTER_NAME")}"
REGION="${GCP_REGION:-$(get_configmap_value "GCP_REGION")}"
PROJECT_ID="${GCP_PROJECT_ID:-$(get_configmap_value "GCP_PROJECT_ID")}"

echo "🚀 Configuring kubectl for GKE Autopilot cluster..."
echo ""
echo "📋 Configuration:"
echo "   Cluster: $CLUSTER_NAME"
echo "   Region: $REGION"
echo "   Project: $PROJECT_ID"
echo ""

# Validate configuration
if [ -z "$CLUSTER_NAME" ]; then
    echo "❌ Error: GKE_CLUSTER_NAME not found"
    echo "   Set it in gitops/base/configmap.yaml or export GKE_CLUSTER_NAME=your-cluster"
    exit 1
fi

if [ -z "$REGION" ]; then
    echo "❌ Error: GCP_REGION not found"
    echo "   Set it in gitops/base/configmap.yaml or export GCP_REGION=your-region"
    exit 1
fi

if [ -z "$PROJECT_ID" ]; then
    echo "❌ Error: GCP_PROJECT_ID not found"
    echo "   Set it in gitops/base/configmap.yaml or export GCP_PROJECT_ID=your-project"
    exit 1
fi

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Error: gcloud CLI is not installed"
    echo "   Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Set the project
echo "🔧 Setting GCP project..."
gcloud config set project "$PROJECT_ID"

# Check if cluster exists
echo ""
echo "🔍 Verifying cluster exists..."
if ! gcloud container clusters describe "$CLUSTER_NAME" --region="$REGION" &> /dev/null; then
    echo "❌ Error: Cluster '$CLUSTER_NAME' not found in region '$REGION'"
    echo ""
    echo "Available clusters:"
    gcloud container clusters list --format="table(name,location,status)"
    exit 1
fi

echo "✅ Cluster found"

# Get cluster credentials
echo ""
echo "🔑 Getting cluster credentials..."
gcloud container clusters get-credentials "$CLUSTER_NAME" \
    --region="$REGION" \
    --project="$PROJECT_ID"

# Install gke-gcloud-auth-plugin if not already installed
if ! command -v gke-gcloud-auth-plugin &> /dev/null; then
    echo ""
    echo "📦 Installing gke-gcloud-auth-plugin..."
    gcloud components install gke-gcloud-auth-plugin --quiet
fi

# Verify connection
echo ""
echo "✅ Cluster connection verified:"
echo ""
kubectl cluster-info
echo ""
kubectl get nodes

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 GKE cluster is ready!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Next steps:"
echo "  1. Create secrets: kubectl apply -f gitops/base/secrets.yaml"
echo "  2. Build images: ./gitops.sh build-gcp"
echo "  3. Deploy services: ./gitops.sh deploy-gcp"
echo "  4. Check status: kubectl get pods -n carsharing"
echo ""
echo "Access your application at: http://$(get_configmap_value "EXTERNAL_IP")"
