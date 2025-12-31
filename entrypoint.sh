#!/bin/bash

# Function to start a service and wait for it
start_service() {
    local service_name=$1
    local jar_path=$2
    local port=$3
    
    echo "🚀 Starting $service_name on port $port..."
    java -jar "$jar_path" --server.port="$port" > "/var/log/$service_name.log" 2>&1 &
    
    # Wait for the service to be ready (optional, but good for Eureka)
    if [ "$service_name" == "eureka-server" ]; then
        echo "⏳ Waiting for Eureka Server to start..."
        while ! curl -s "http://localhost:$port/actuator/health" | grep -q "UP"; do
            sleep 2
        done
        echo "✅ Eureka Server is UP!"
    fi
}

# Create log directory
mkdir -p /var/log

# 1. Start Eureka Server
start_service "eureka-server" "/app/eureka-server.jar" 8761

# 2. Start other services in background
start_service "auth-service" "/app/auth-service.jar" 8089
start_service "user-service" "/app/user-service.jar" 8081
start_service "vehicle-service" "/app/vehicle-service.jar" 8082
start_service "trip-service" "/app/trip-service.jar" 8083
start_service "ride-service" "/app/ride-service.jar" 8084
start_service "notification-service" "/app/notification-service.jar" 8085

# 3. Start API Gateway last
start_service "api-gateway" "/app/api-gateway.jar" 8080

echo "🌟 All services started!"
echo "Log files are available in /var/log/"

# Keep the script running to prevent the container from exiting
# We'll tail the gateway logs to keep the process alive and show output
tail -f /var/log/api-gateway.log
