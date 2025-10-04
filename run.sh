#!/bin/bash
set -e

echo "🧱 Building all JARs first..."
./build_all_jars.sh

echo "🛠️ Clear containers..."
podman-compose down -v

echo "🚀 Starting containers..."
podman-compose up --build -d
