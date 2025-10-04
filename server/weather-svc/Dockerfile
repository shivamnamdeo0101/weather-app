# ===============================================
# Weather App Dockerfile
# Base Image: OpenJDK 21 Slim (JDK)
# Purpose: Run pre-built JAR for weather-svc
# ===============================================
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy the pre-built JAR
COPY target/weather-svc-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
