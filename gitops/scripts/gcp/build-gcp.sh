#!/bin/bash

# Script to build and push Docker images to GCR
# Usage: ./build-gcp.sh [service1 service2 ...] or ./build-gcp.sh (for all services)

set -e

# Get the directory of this script and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Default tag
TAG="${GCP_IMAGE_TAG:-latest}"
REGISTRY="gcr.io"

# All available services
ALL_SERVICES=(
    "auth-service"
    "user-service"
    "vehicle-service"
    "trip-service"
    "ride-service"
    "notification-service"
    "api-gateway"
)

# Function to extract value from ConfigMap YAML
get_configmap_value() {
    local key=$1
    local configmap_file="$PROJECT_ROOT/gitops/base/configmap.yaml"
    
    if [ -f "$configmap_file" ]; then
        # Extract value using grep and sed
        grep "^  $key:" "$configmap_file" | sed 's/.*: "\(.*\)"/\1/' | tr -d '"'
    fi
}

# Get project ID from environment or ConfigMap
if [ -z "$GCP_PROJECT_ID" ]; then
    GCP_PROJECT_ID=$(get_configmap_value "GCP_PROJECT_ID")
fi

if [ -z "$GCP_PROJECT_ID" ]; then
    echo "❌ Error: GCP_PROJECT_ID not found"
    echo "   Set it with: export GCP_PROJECT_ID=your-project-id"
    echo "   Or configure it in: gitops/base/configmap.yaml"
    exit 1
fi

# Determine which services to build
if [ $# -eq 0 ]; then
    # No arguments - build all services
    SERVICES_TO_BUILD=("${ALL_SERVICES[@]}")
    echo "📦 Building all services..."
else
    # Validate provided service names
    SERVICES_TO_BUILD=()
    for service in "$@"; do
        if [[ " ${ALL_SERVICES[@]} " =~ " ${service} " ]]; then
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

echo ""
echo "🐳 Build Configuration:"
echo "   Registry: $REGISTRY"
echo "   Project: $GCP_PROJECT_ID"
echo "   Tag: $TAG"
echo "   Services: ${SERVICES_TO_BUILD[*]}"
echo ""

# Configure Docker for GCR
echo "🔑 Configuring Docker for GCR..."
if ! gcloud auth configure-docker gcr.io --quiet 2>/dev/null; then
    echo "⚠️  Warning: Could not configure Docker auth (may already be configured)"
fi

# Change to project root for Docker build context
cd "$PROJECT_ROOT"

# Build and push each service
SUCCESS_COUNT=0
FAIL_COUNT=0
FAILED_SERVICES=()

for service in "${SERVICES_TO_BUILD[@]}"; do
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 Building $service..."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    IMAGE_NAME="$REGISTRY/$GCP_PROJECT_ID/$service:$TAG"
    
    if docker build \
        -f "./$service/Dockerfile" \
        -t "$IMAGE_NAME" \
        . ; then
        
        echo ""
        echo "⬆️  Pushing $service to GCR..."
        
        if docker push "$IMAGE_NAME"; then
            echo "✅ $service pushed successfully"
            ((SUCCESS_COUNT++))
        else
            echo "❌ Failed to push $service"
            FAILED_SERVICES+=("$service")
            ((FAIL_COUNT++))
        fi
    else
        echo "❌ Failed to build $service"
        FAILED_SERVICES+=("$service")
        ((FAIL_COUNT++))
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Build Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   ✅ Successful: $SUCCESS_COUNT"
echo "   ❌ Failed: $FAIL_COUNT"

if [ $FAIL_COUNT -gt 0 ]; then
    echo ""
    echo "   Failed services: ${FAILED_SERVICES[*]}"
    exit 1
fi

echo ""
echo "🎉 All images built and pushed successfully!"
echo ""
echo "Images:"
for service in "${SERVICES_TO_BUILD[@]}"; do
    echo "  - $REGISTRY/$GCP_PROJECT_ID/$service:$TAG"
done
echo ""
echo "Next steps:"
echo "  1. Deploy to GKE: ./gitops.sh deploy-gcp"
echo "  2. Check status: kubectl get pods -n carsharing"
