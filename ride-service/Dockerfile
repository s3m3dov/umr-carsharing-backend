# syntax=docker/dockerfile:1.4
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /root

# Copy only POM files first for better layer caching
COPY pom.xml .
COPY common/pom.xml ./common/
COPY ride-service/pom.xml ./ride-service/

# Configure Maven to use Google's mirror for better reliability
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?> \
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 \
    http://maven.apache.org/xsd/settings-1.0.0.xsd"> \
    <mirrors> \
    <mirror> \
    <id>google-maven-central</id> \
    <name>Google Maven Central</name> \
    <url>https://maven-central-eu.storage-download.googleapis.com/maven2/</url> \
    <mirrorOf>central</mirrorOf> \
    </mirror> \
    </mirrors> \
    </settings>' > /root/.m2/settings.xml

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -N install && \
    cd common && mvn dependency:go-offline -DskipTests

# Copy source code
COPY common/src ./common/src
COPY ride-service/src ./ride-service/src

# Build with cached dependencies
RUN --mount=type=cache,target=/root/.m2 \
    cd common && mvn clean install -DskipTests && \
    cd ../ride-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Install CA certificates for MongoDB Atlas SSL/TLS connections
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates ca-certificates-java && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /root/ride-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]