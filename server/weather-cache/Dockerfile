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
    mvn clean package -DskipTests && \
    apt-get clean

# ==============================
# Stage 2: Final lightweight image
# ==============================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/weather-cache-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8081

# Use environment variables for Spring Boot and Redis
ENTRYPOINT ["java","-jar","app.jar"]


