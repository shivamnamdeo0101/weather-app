# ==============================
# Stage 1: Build the JAR
# ==============================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build the project
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -Pprod -DskipTests && \
    apt-get clean

# ==============================
# Stage 2: Create final lightweight image
# ==============================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/weather-svc-0.0.1-SNAPSHOT.jar app.jar

# Expose application port (from properties)
EXPOSE 8081

# Use only environment variables, no defaults
ENTRYPOINT ["java", "-jar", "app.jar"]
