# ================= BUILD STAGE =================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Root pom (for multi-module)
COPY pom.xml .

# Modules
COPY common common
COPY eureka-server eureka-server
COPY auth-service auth-service
COPY user-service user-service
COPY vehicle-service vehicle-service
COPY trip-service trip-service
COPY ride-service ride-service
COPY api-gateway api-gateway
COPY notification-service notification-service

# Build everything
RUN mvn -B -DskipTests package


# ================= RUNTIME STAGE =================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install supervisor
RUN apt-get update && \
    apt-get install -y supervisor curl && \
    rm -rf /var/lib/apt/lists/*

# Copy JARs
COPY --from=build /build/eureka-server/target/*.jar eureka.jar
COPY --from=build /build/auth-service/target/*.jar auth.jar
COPY --from=build /build/user-service/target/*.jar user.jar
COPY --from=build /build/vehicle-service/target/*.jar vehicle.jar
COPY --from=build /build/trip-service/target/*.jar trip.jar
COPY --from=build /build/ride-service/target/*.jar ride.jar
COPY --from=build /build/api-gateway/target/*.jar gateway.jar
COPY --from=build /build/notification-service/target/*.jar notification.jar

# Supervisor config
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose same ports as compose
EXPOSE 8761 8080 8081 8082 8083 8084 8085 8089

CMD ["supervisord", "-n"]
