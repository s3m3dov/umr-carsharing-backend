#!/bin/bash

# Script to build Docker images and load them into Kind cluster
# Usage: ./build-for-kind.sh [tag]

set -e

TAG="${1:-local}"
CLUSTER_NAME="carsharing-local"

SERVICES=(
    "auth-service"
    "user-service"
    "vehicle-service"
    "trip-service"
    "ride-service"
    "notification-service"
    "api-gateway"
)

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
for service in "${SERVICES[@]}"; do
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
for service in "${SERVICES[@]}"; do
    echo "  - $service:$TAG"
done
echo ""
echo "Next step:"
echo "   ./gitops/gitops.sh apply local"
