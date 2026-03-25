#!/bin/bash

# Script to configure kubectl for existing GKE Autopilot cluster
# Usage: ./connect-gcp.sh

set -e

# Get the directory of this script and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../common.sh"
PROJECT_ROOT=$(get_project_root "$SCRIPT_DIR" 3)

# Read configuration from ConfigMap or environment variables
CLUSTER_NAME="${GKE_CLUSTER_NAME:-$(get_configmap_value "GKE_CLUSTER_NAME" "$PROJECT_ROOT")}"
REGION="${GCP_REGION:-$(get_configmap_value "GCP_REGION" "$PROJECT_ROOT")}"
PROJECT_ID="${GCP_PROJECT_ID:-$(get_configmap_value "GCP_PROJECT_ID" "$PROJECT_ROOT")}"

echo "🚀 Configuring kubectl for GKE Autopilot cluster..."
echo ""
echo "📋 Configuration:"
echo "   Cluster: $CLUSTER_NAME"
echo "   Region: $REGION"
echo "   Project: $PROJECT_ID"
echo ""

# Validate configuration
validate_config "GKE_CLUSTER_NAME" "$CLUSTER_NAME" "gitops/base/configmap.yaml"
validate_config "GCP_REGION" "$REGION" "gitops/base/configmap.yaml"
validate_config "GCP_PROJECT_ID" "$PROJECT_ID" "gitops/base/configmap.yaml"

# Check required commands
require_command "gcloud" "https://cloud.google.com/sdk/docs/install"

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
print_separator
echo "🎉 GKE cluster is ready!"
print_separator
echo ""
echo "Next steps:"
echo "  1. Create secrets: ./gitops.sh gcp secrets"
echo "  2. Build images: ./gitops.sh gcp build"
echo "  3. Deploy services: ./gitops.sh gcp deploy"
echo "  4. Check status: kubectl get pods -n carsharing"
echo ""
echo "Access your application at: http://$(get_configmap_value "EXTERNAL_IP" "$PROJECT_ROOT")"
