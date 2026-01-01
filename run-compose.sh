#!/bin/bash

# Docker Build Helper Script
# This script enables BuildKit and builds all services with optimized caching

set -e

echo "🚀 Building carsharing-backend with BuildKit optimizations..."
echo ""

# Enable BuildKit for this build
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Build and start all services
echo "📦 Building all services (initial build may take 2-3 minutes)..."
docker-compose up -d --build

echo ""
echo "✅ Build complete!"
echo ""
echo "📊 Container status:"
docker-compose ps

echo ""
echo "💡 Tip: Subsequent builds will be much faster (~30-45 seconds) by utilizing BuildKit cache!"
echo "💡 To view logs: docker-compose logs -f [service-name]"
echo "💡 To stop all services: docker-compose down"
