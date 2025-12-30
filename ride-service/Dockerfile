FROM maven:3.9.6-eclipse-temurin-21 AS build

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

# Install parent POM and common module, then build ride-service
RUN mvn -N install && \
    cd common && \
    mvn clean install -DskipTests && \
    cd ../ride-service && \
    mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /root/ride-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]