#!/bin/bash

# Docker Build Helper Script (Root Dockerfile Version)
# This script builds the entire backend stack into a single image

set -e

IMAGE_NAME="carsharing-backend"

echo "🚀 Building $IMAGE_NAME using root Dockerfile..."
echo ""

# Enable BuildKit
export DOCKER_BUILDKIT=1

# Build the image
echo "📦 Building the main image..."
docker build -t "$IMAGE_NAME" .

echo ""
echo "✅ Build complete!"
echo "🐳 Image: $IMAGE_NAME"
echo ""
echo "💡 To run the container:"
echo "   docker run -d --name $IMAGE_NAME -p 8080:8080 -p 8761:8761 $IMAGE_NAME"
