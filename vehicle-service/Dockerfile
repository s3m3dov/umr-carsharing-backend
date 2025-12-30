# syntax=docker/dockerfile:1.4
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /root

# Copy only POM files first for better layer caching
COPY pom.xml .
COPY common/pom.xml ./common/
COPY vehicle-service/pom.xml ./vehicle-service/

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 \
  mvn -N install && \
  cd common && mvn dependency:go-offline -DskipTests

# Copy source code
COPY common/src ./common/src
COPY vehicle-service/src ./vehicle-service/src

# Build with cached dependencies
RUN --mount=type=cache,target=/root/.m2 \
  cd common && mvn clean install -DskipTests && \
  cd ../vehicle-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Install CA certificates for MongoDB Atlas SSL/TLS connections
RUN apt-get update && \
  apt-get install -y --no-install-recommends ca-certificates ca-certificates-java && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /root/vehicle-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]