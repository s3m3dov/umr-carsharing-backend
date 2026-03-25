# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copy the entire project
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /build/target/*.jar application.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Default to MongoDB profile, can be overridden at runtime
ENV SPRING_PROFILES_ACTIVE=mongodb

# Run the application
ENTRYPOINT ["java", "-jar", "application.jar"]
