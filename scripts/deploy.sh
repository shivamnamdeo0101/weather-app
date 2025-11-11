#!/usr/bin/env bash
set -euo pipefail
# deploy.sh
# Usage: ./scripts/deploy.sh <namespace> <image-tag>
# Example: ./scripts/deploy.sh dev dev-83
#
# This script dynamically replaces placeholders and deploys to Kubernetes
# It matches the GitHub Actions workflow behavior

if [ $# -lt 2 ]; then
  echo "❌ Usage: $0 <namespace> <image-tag>"
  echo ""
  echo "Examples:"
  echo "  ./scripts/deploy.sh dev dev-83"
  echo "  ./scripts/deploy.sh test test-42"
  echo "  ./scripts/deploy.sh prod prod-1"
  echo ""
  exit 1
fi

NAMESPACE="$1"
IMAGE_TAG="$2"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
K8S_DIR="$ROOT_DIR/k8s"
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 DEPLOYMENT STARTED"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  📌 Namespace:   $NAMESPACE"
echo "  🏷️  Image Tag:   $IMAGE_TAG"
echo "  📁 Manifests:   $K8S_DIR"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Step 1: Create namespace
echo "📦 Step 1: Creating namespace '$NAMESPACE'..."
if kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - &>/dev/null; then
  echo "   ✅ Namespace ready"
else
  echo "   ✅ Namespace already exists"
fi
echo ""

# Step 2: Replace placeholders
echo "🔄 Step 2: Replacing placeholders in manifests..."
count=0
for manifest in "$K8S_DIR"/*.yaml; do
  TEMP_FILE="$TEMP_DIR/$(basename "$manifest")"
  sed "s|NAMESPACE_PLACEHOLDER|$NAMESPACE|g; s|IMAGE_TAG_PLACEHOLDER|$IMAGE_TAG|g; s|ENV_PLACEHOLDER|$NAMESPACE|g" "$manifest" > "$TEMP_FILE"
  count=$((count + 1))
  echo "   ✓ $(basename "$manifest")"
done
echo "   ✅ Processed $count manifest(s)"
echo ""

# Step 3: Apply manifests
echo "📋 Step 3: Applying manifests to namespace '$NAMESPACE'..."
for manifest in "$TEMP_DIR"/*.yaml; do
  kubectl apply -f "$manifest" -n "$NAMESPACE" &>/dev/null
  echo "   ✓ $(basename "$manifest")"
done
echo "   ✅ All manifests applied"
echo ""

# Step 4: Wait for deployments
echo "⏳ Step 4: Waiting for deployments to be ready..."
echo ""

deployments=("redis-db" "weather-svc" "weather-cache" "weather-app")
timeouts=("2m" "5m" "5m" "3m")

for i in "${!deployments[@]}"; do
  dep="${deployments[$i]}"
  timeout="${timeouts[$i]}"
  
  echo "  ⏳ Waiting for $dep (timeout: $timeout)..."
  if kubectl rollout status deployment/"$dep" --timeout="$timeout" -n "$NAMESPACE" &>/dev/null; then
    echo "     ✅ $dep is ready"
  else
    echo "     ❌ $dep failed to rollout or timed out"
    echo ""
    echo "     📋 Pod Status:"
    kubectl get pods -l app="$dep" -n "$NAMESPACE" 2>/dev/null || echo "     No pods found"
    echo ""
    echo "     🔍 Pod Description:"
    kubectl describe pods -l app="$dep" -n "$NAMESPACE" 2>/dev/null | tail -20 || echo "     No description available"
    echo ""
    exit 1
  fi
done
echo ""

# Step 5: Show status
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ DEPLOYMENT SUCCESSFUL!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📊 Current Pod Status:"
kubectl get pods -o wide -n "$NAMESPACE"
echo ""

echo "📊 Current Services:"
kubectl get svc -o wide -n "$NAMESPACE"
echo ""

echo "📊 Current Deployments:"
kubectl get deployments -o wide -n "$NAMESPACE"
echo ""

echo "📝 Useful Commands:"
echo "  • View all resources:     kubectl get all -n $NAMESPACE"
echo "  • View logs:              kubectl logs -f deployment/weather-cache -n $NAMESPACE"
echo "  • Describe pod:           kubectl describe pod <pod-name> -n $NAMESPACE"
echo "  • Port forward:           kubectl port-forward svc/weather-app 3001:3001 -n $NAMESPACE"
echo "  • Delete deployment:      ./scripts/delete.sh $NAMESPACE"
echo ""
