#!/bin/bash

# Script to create and configure a GKE Autopilot cluster
# Usage: ./deploy-gke.sh [cluster-name] [region]

set -e

CLUSTER_NAME="${1:-carsharing-cluster}"
REGION="${2:-us-central1}"
PROJECT_ID="${GCP_PROJECT_ID:-}"

echo "🚀 Creating GKE Autopilot cluster..."
echo "   Cluster: $CLUSTER_NAME"
echo "   Region: $REGION"
echo "   Project: $PROJECT_ID"
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Error: gcloud CLI is not installed"
    echo "   Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if project ID is set
if [ -z "$PROJECT_ID" ]; then
    echo "❌ Error: GCP_PROJECT_ID environment variable is not set"
    echo "   Set it with: export GCP_PROJECT_ID=your-project-id"
    exit 1
fi

# Set the project
gcloud config set project "$PROJECT_ID"

# Check if cluster already exists
if gcloud container clusters describe "$CLUSTER_NAME" --region="$REGION" &> /dev/null; then
    echo "✅ Cluster $CLUSTER_NAME already exists"
else
    echo "📦 Creating Autopilot cluster (this may take 5-10 minutes)..."
    gcloud container clusters create-auto "$CLUSTER_NAME" \
        --region="$REGION" \
        --project="$PROJECT_ID"
    
    echo "✅ Cluster created successfully"
fi

# Get cluster credentials
echo "🔑 Getting cluster credentials..."
gcloud container clusters get-credentials "$CLUSTER_NAME" \
    --region="$REGION" \
    --project="$PROJECT_ID"

# Install gke-gcloud-auth-plugin if not already installed
if ! command -v gke-gcloud-auth-plugin &> /dev/null; then
    echo "📦 Installing gke-gcloud-auth-plugin..."
    gcloud components install gke-gcloud-auth-plugin
fi

# Verify connection
echo "✅ Cluster connection verified:"
kubectl cluster-info

echo ""
echo "🎉 GKE cluster is ready!"
echo ""
echo "Next steps:"
echo "1. Create secrets: ./scripts/create-secrets.sh"
echo "2. Deploy services: ./gitops/gitops.sh apply production"
echo "3. Check status: kubectl get pods -n carsharing"
