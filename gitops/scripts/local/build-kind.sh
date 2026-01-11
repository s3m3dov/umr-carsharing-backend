#!/bin/bash

# Script to build Docker images and load them into Kind cluster
# Usage: ./build-kind.sh [tag]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../common.sh"
PROJECT_ROOT=$(get_project_root "$SCRIPT_DIR" 3)

TAG="${1:-local}"
CLUSTER_NAME="carsharing-local"

echo "🐳 Building Docker images for Kind..."
echo "   Tag: $TAG"
echo "   Cluster: $CLUSTER_NAME"
echo ""

# Check if Kind cluster exists
if ! kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
    echo "❌ Error: Kind cluster '$CLUSTER_NAME' not found"
    echo "   Create it first with: ./scripts/local/setup-kind.sh"
    exit 1
fi

# Build and load each service
for service in "${ALL_SERVICES[@]}"; do
    echo ""
    echo "📦 Building $service..."
    
    IMAGE_NAME="$service:$TAG"
    
    docker build \
        -f "./$service/Dockerfile" \
        -t "$IMAGE_NAME" \
        .
    
    echo "⬆️  Loading $service into Kind cluster..."
    kind load docker-image "$IMAGE_NAME" --name "$CLUSTER_NAME"
    
    echo "✅ $service loaded successfully"
done

echo ""
echo "🎉 All images built and loaded into Kind!"
echo ""
echo "Images in cluster:"
for service in "${ALL_SERVICES[@]}"; do
    echo "  - $service:$TAG"
done
echo ""
echo "Next step:"
echo "   ./gitops/gitops.sh apply local"
