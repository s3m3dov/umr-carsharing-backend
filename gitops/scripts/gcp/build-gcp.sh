#!/bin/bash

# Script to build and push Docker images to GCR
# Usage: ./build-gcp.sh [service1 service2 ...] or ./build-gcp.sh (for all services)

set -e

# Get the directory of this script and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../common.sh"
PROJECT_ROOT=$(get_project_root "$SCRIPT_DIR" 3)

# Default tag
TAG="${GCP_IMAGE_TAG:-latest}"
REGISTRY="eu.gcr.io"

# Get project ID from environment or ConfigMap
GCP_PROJECT_ID="${GCP_PROJECT_ID:-$(get_configmap_value "GCP_PROJECT_ID" "$PROJECT_ROOT")}"
validate_config "GCP_PROJECT_ID" "$GCP_PROJECT_ID" "gitops/base/configmap.yaml"

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
if ! gcloud auth configure-docker eu.gcr.io --quiet 2>/dev/null; then
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
    print_separator
    echo "📦 Building $service..."
    print_separator
    
    IMAGE_NAME="$REGISTRY/$GCP_PROJECT_ID/$service:$TAG"
    
    if docker build \
        --platform linux/amd64 \
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
print_separator
echo "📊 Build Summary"
print_separator
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
echo "  1. Deploy to GKE: ./gitops.sh gcp deploy"
echo "  2. Check status: kubectl get pods -n carsharing"
