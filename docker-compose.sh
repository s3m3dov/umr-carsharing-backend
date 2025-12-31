#!/bin/bash

# Docker Build Helper Script (Compose Version)
# This script enables BuildKit and builds all services using docker-compose

set -e

echo "🚀 Building carsharing-backend with docker-compose..."
echo ""

# Enable BuildKit
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
echo "💡 To view logs: docker-compose logs -f [service-name]"
echo "💡 To stop all services: docker-compose down"
