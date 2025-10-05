#!/bin/bash
#File name run.sh
set -e

echo "🧱 Building all JARs first..."
./build_all_jars.sh

# Detect container engine and compose command (prefer Podman if available)
if command -v podman-compose >/dev/null 2>&1; then
    COMPOSE_CMD="podman-compose"
    IMAGE_PRUNE_CMD="podman image prune --force"
elif command -v docker >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
    IMAGE_PRUNE_CMD="docker image prune --force"
else
    echo "❌ Neither Docker nor podman-compose found. Please install Docker Desktop or Podman."
    exit 1
fi

echo "🛠️ Bringing down existing containers (volumes removed)..."
$COMPOSE_CMD down -v || true

# This removes stopped containers, unused networks, and all build cache.
echo "🛠️ Removing unused/cache images..."
eval "$IMAGE_PRUNE_CMD"

echo "🚀 Starting containers..."
$COMPOSE_CMD up --build -d