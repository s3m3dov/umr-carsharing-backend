FROM maven:3.8.4-openjdk-17 AS build

# Copy the entire project to build the parent POM first
WORKDIR /root
COPY pom.xml .
COPY common ./common
COPY eureka-server ./eureka-server
COPY api-gateway ./api-gateway
COPY auth-service ./auth-service
COPY user-service ./user-service
COPY vehicle-service ./vehicle-service
COPY ride-service ./ride-service
COPY notification-service ./notification-service
COPY trip-service ./trip-service

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

# Install parent POM and common module, then build trip-service WITH RETRY
RUN (mvn -N install || (sleep 10 && mvn -N install) || (sleep 30 && mvn -N install)) && \
    cd common && \
    (mvn clean install -DskipTests || (sleep 10 && mvn clean install -DskipTests) || (sleep 30 && mvn clean install -DskipTests)) && \
    cd ../trip-service && \
    (mvn clean package -DskipTests || (sleep 10 && mvn clean package -DskipTests) || (sleep 30 && mvn clean package -DskipTests))

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /root/trip-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]