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

# Install parent POM and common module, then build api-gateway
RUN mvn -N install && \
    cd common && \
    mvn clean install -DskipTests && \
    cd ../api-gateway && \
    mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /root/notification-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]