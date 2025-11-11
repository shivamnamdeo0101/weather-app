#!/usr/bin/env bash
set -euo pipefail
# delete.sh
# Usage: ./scripts/delete.sh <namespace>
# Example: ./scripts/delete.sh dev
#
# This script deletes all resources created by deploy.sh from a namespace
# and optionally deletes the namespace itself

if [ $# -lt 1 ]; then
  echo "❌ Usage: $0 <namespace>"
  echo ""
  echo "Examples:"
  echo "  ./scripts/delete.sh dev"
  echo "  ./scripts/delete.sh test"
  echo "  ./scripts/delete.sh prod"
  echo ""
  exit 1
fi

NAMESPACE="$1"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
K8S_DIR="$ROOT_DIR/k8s"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗑️  DELETION STARTED"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  📌 Namespace:   $NAMESPACE"
echo "  📁 Manifests:   $K8S_DIR"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if namespace exists
if ! kubectl get namespace "$NAMESPACE" &>/dev/null; then
  echo "⚠️  Namespace '$NAMESPACE' does not exist"
  echo "✅ Nothing to delete"
  exit 0
fi

# Step 1: Delete deployments
echo "🗑️  Step 1: Deleting deployments..."
deployments=("redis-db" "weather-svc" "weather-cache" "weather-app")
for dep in "${deployments[@]}"; do
  if kubectl delete deployment "$dep" -n "$NAMESPACE" &>/dev/null; then
    echo "   ✓ Deleted deployment: $dep"
  else
    echo "   ⚠️  Deployment $dep not found (already deleted?)"
  fi
done
echo ""

# Step 2: Delete services
echo "🗑️  Step 2: Deleting services..."
services=("redis-db" "weather-svc" "weather-cache" "weather-app")
for svc in "${services[@]}"; do
  if kubectl delete service "$svc" -n "$NAMESPACE" &>/dev/null; then
    echo "   ✓ Deleted service: $svc"
  else
    echo "   ⚠️  Service $svc not found (already deleted?)"
  fi
done
echo ""

# Step 3: Delete ConfigMaps
echo "🗑️  Step 3: Deleting ConfigMaps..."
configmaps=("server-weather-cache--env" "server-weather-svc--env")
for cm in "${configmaps[@]}"; do
  if kubectl delete configmap "$cm" -n "$NAMESPACE" &>/dev/null; then
    echo "   ✓ Deleted ConfigMap: $cm"
  else
    echo "   ⚠️  ConfigMap $cm not found (already deleted?)"
  fi
done
echo ""

# Step 4: Delete PersistentVolumeClaims
echo "🗑️  Step 4: Deleting PersistentVolumeClaims..."
pvcs=("redis-data")
for pvc in "${pvcs[@]}"; do
  if kubectl delete pvc "$pvc" -n "$NAMESPACE" &>/dev/null; then
    echo "   ✓ Deleted PVC: $pvc"
  else
    echo "   ⚠️  PVC $pvc not found (already deleted?)"
  fi
done
echo ""

# Step 5: Delete namespace
echo "🗑️  Step 5: Deleting namespace '$NAMESPACE'..."
if kubectl delete namespace "$NAMESPACE" &>/dev/null; then
  echo "   ✅ Namespace deleted"
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "✅ DELETION SUCCESSFUL!"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
else
  echo "   ⚠️  Failed to delete namespace"
  exit 1
fi
echo ""

# Verify
echo "✓ Remaining namespaces:"
kubectl get namespace | grep -v NAME || echo "  (none)"
echo ""
