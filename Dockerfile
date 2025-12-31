# syntax=docker/dockerfile:1.4

# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /root

# Copy only POM files first for better layer caching
COPY pom.xml .
COPY common/pom.xml ./common/
COPY eureka-server/pom.xml ./eureka-server/
COPY auth-service/pom.xml ./auth-service/
COPY user-service/pom.xml ./user-service/
COPY vehicle-service/pom.xml ./vehicle-service/
COPY trip-service/pom.xml ./trip-service/
COPY ride-service/pom.xml ./ride-service/
COPY api-gateway/pom.xml ./api-gateway/
COPY notification-service/pom.xml ./notification-service/

# Download dependencies (cached layer)
RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn dependency:go-offline -DskipTests

# Copy source code
COPY . .

# Build all modules
RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-jammy

# Install curl for health checks and ca-certificates for MongoDB Atlas
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates ca-certificates-java && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JARs from build stage
COPY --from=build /root/eureka-server/target/*.jar /app/eureka-server.jar
COPY --from=build /root/auth-service/target/*.jar /app/auth-service.jar
COPY --from=build /root/user-service/target/*.jar /app/user-service.jar
COPY --from=build /root/vehicle-service/target/*.jar /app/vehicle-service.jar
COPY --from=build /root/trip-service/target/*.jar /app/trip-service.jar
COPY --from=build /root/ride-service/target/*.jar /app/ride-service.jar
COPY --from=build /root/api-gateway/target/*.jar /app/api-gateway.jar
COPY --from=build /root/notification-service/target/*.jar /app/notification-service.jar

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Expose ports
EXPOSE 8080 8761 8081 8082 8083 8084 8085 8089

# Default environment variables
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE="http://localhost:8761/eureka/"

ENTRYPOINT ["/app/entrypoint.sh"]
