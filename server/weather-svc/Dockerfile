# ==============================
# Stage 1: Build the JAR
# ==============================
FROM eclipse-temurin:21-jdk-jammy AS build

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven, build JAR, then clean up to reduce image size
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -Pprod -DskipTests && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# ==============================
# Stage 2: Runtime (lightweight)
# ==============================
FROM eclipse-temurin:21-jre-jammy AS runtime

# Working directory
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/weather-svc-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
