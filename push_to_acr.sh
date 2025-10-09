#!/bin/bash

# ===============================================
# 1️⃣ Set Variables
# ===============================================
ACR_NAME="shivamweatherapp"
ACR_LOGIN_SERVER="$ACR_NAME.azurecr.io"

# Services
SERVICES=("weather-svc" "weather-cache")

# ===============================================
# 2️⃣ Azure Login
# ===============================================
echo "Logging in to Azure..."
az login --use-device-code

echo "Logging in to ACR..."
az acr login --name $ACR_NAME

# ===============================================
# 3️⃣ Build & Tag Docker Images
# ===============================================
for SERVICE in "${SERVICES[@]}"; do
  IMAGE_TAG="$ACR_LOGIN_SERVER/$SERVICE:latest"
  echo "Building $SERVICE..."
  docker build -t $IMAGE_TAG ./server/$SERVICE
done

# ===============================================
# 4️⃣ Verify Local Images
# ===============================================
echo "Verifying locally built images..."
docker images | grep "$ACR_LOGIN_SERVER"

# ===============================================
# 5️⃣ Push Images to ACR
# ===============================================
for SERVICE in "${SERVICES[@]}"; do
  IMAGE_TAG="$ACR_LOGIN_SERVER/$SERVICE:latest"
  echo "Pushing $IMAGE_TAG to ACR..."
  docker push $IMAGE_TAG
done

# ===============================================
# 6️⃣ Verify in ACR
# ===============================================
echo "Listing images in ACR..."
az acr repository list --name $ACR_NAME --output table

echo "✅ All images built and pushed successfully!"
