#!/bin/bash
#File name build_all_jars.sh

set -e

# Base directory
BASE_DIR=$(pwd)

# -----------------------------
# 1. Build weather-svc JAR
# -----------------------------
echo "Building weather-svc JAR..."
cd "$BASE_DIR/server/weather-svc"
./mvnw clean package -DskipTests

SVC_JAR=$(ls target/*.jar | head -n 1)
if [ -f "$SVC_JAR" ]; then
    echo "✅ weather-svc JAR built: $SVC_JAR"
else
    echo "❌ Failed to build weather-svc JAR!"
    exit 1
fi

# -----------------------------
# 2. Build weather-cache JAR
# -----------------------------
echo "Building weather-cache JAR..."
cd "$BASE_DIR/server/weather-cache"
./mvnw clean package -DskipTests

CACHE_JAR=$(ls target/*.jar | head -n 1)
if [ -f "$CACHE_JAR" ]; then
    echo "✅ weather-cache JAR built: $CACHE_JAR"
else
    echo "❌ Failed to build weather-cache JAR!"
    exit 1
fi

# -----------------------------
# Done
# -----------------------------
echo "✅ Both JARs built successfully!"
