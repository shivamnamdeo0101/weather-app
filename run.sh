#!/bin/bash
#File name run.sh
set -e

echo "🧱 Building all JARs first..."
./build_all_jars.sh

echo "🛠️ down -v containers..."
podman-compose down -v

# This removes stopped containers, unused networks, and all build cache.
echo "🛠️ Remove unused/cache containers..."
podman image prune --force


echo "🚀 Starting containers..."
podman-compose up --build -d