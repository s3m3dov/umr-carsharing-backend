#!/bin/bash

# Script to build Docker images and load them into Kind cluster
# Usage: ./build-kind.sh [tag] [service1 service2 ...]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../common.sh"
PROJECT_ROOT=$(get_project_root "$SCRIPT_DIR" 3)

TAG="${1:-local}"
shift || true
CLUSTER_NAME="carsharing-local"

# Determine which services to build
if [ $# -eq 0 ]; then
    SERVICES_TO_BUILD=("${ALL_SERVICES[@]}")
    echo "📦 Building all services..."
else
    SERVICES_TO_BUILD=()
    for service in "$@"; do
        if [[ " ${ALL_SERVICES[*]} " =~ " ${service} " ]]; then
            SERVICES_TO_BUILD+=("$service")
        else
            echo "⚠️  Warning: Unknown service '$service', skipping..."
            echo "   Available services: ${ALL_SERVICES[*]}"
        fi
    done

    if [ ${#SERVICES_TO_BUILD[@]} -eq 0 ]; then
        echo "❌ Error: No valid services specified"
        echo "   Available services: ${ALL_SERVICES[*]}"
        exit 1
    fi

    echo "📦 Building selected services: ${SERVICES_TO_BUILD[*]}"
fi

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
for service in "${SERVICES_TO_BUILD[@]}"; do
    echo ""
    echo "📦 Building $service..."
    
    IMAGE_NAME="$service:$TAG"
    
    docker build \
        --pull \
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
for service in "${SERVICES_TO_BUILD[@]}"; do
    echo "  - $service:$TAG"
done
echo ""
echo "Next step:"
echo "   ./gitops.sh local deploy"
