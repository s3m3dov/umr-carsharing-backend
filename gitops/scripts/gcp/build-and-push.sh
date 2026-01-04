#!/bin/bash

# Script to build and push Docker images to GCR
# Usage: ./build-and-push.sh [tag]

set -e

TAG="${1:-latest}"
PROJECT_ID="${GCP_PROJECT_ID:-}"
REGISTRY="gcr.io"

SERVICES=(
    "auth-service"
    "user-service"
    "vehicle-service"
    "trip-service"
    "ride-service"
    "notification-service"
    "api-gateway"
)

echo "🐳 Building and pushing Docker images..."
echo "   Registry: $REGISTRY"
echo "   Project: $PROJECT_ID"
echo "   Tag: $TAG"
echo ""

# Check if project ID is set
if [ -z "$PROJECT_ID" ]; then
    echo "❌ Error: GCP_PROJECT_ID environment variable is not set"
    echo "   Set it with: export GCP_PROJECT_ID=your-project-id"
    exit 1
fi

# Configure Docker for GCR
echo "🔑 Configuring Docker for GCR..."
gcloud auth configure-docker gcr.io

# Build and push each service
for service in "${SERVICES[@]}"; do
    echo ""
    echo "📦 Building $service..."
    
    IMAGE_NAME="$REGISTRY/$PROJECT_ID/$service:$TAG"
    
    docker build \
        -f "./$service/Dockerfile" \
        -t "$IMAGE_NAME" \
        .
    
    echo "⬆️  Pushing $service to $IMAGE_NAME..."
    docker push "$IMAGE_NAME"
    
    echo "✅ $service pushed successfully"
done

echo ""
echo "🎉 All images built and pushed successfully!"
echo ""
echo "Images:"
for service in "${SERVICES[@]}"; do
    echo "  - $REGISTRY/$PROJECT_ID/$service:$TAG"
done
