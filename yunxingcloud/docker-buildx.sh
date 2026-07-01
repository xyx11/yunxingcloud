#!/bin/bash
set -e
# ============================================
# Docker 多架构构建 (amd64 + arm64)
# 用法:
#   ./docker-buildx.sh              # 本地构建
#   ./docker-buildx.sh push         # 构建并推送到 GHCR
# ============================================

REGISTRY="${REGISTRY:-ghcr.io/xyx}"
TAG="${TAG:-latest}"
PLATFORMS="linux/amd64,linux/arm64"
SERVICES=("core" "usercenter" "payment" "order" "inventory" "gateway")
PORTS=(8080 8081 8083 8084 8085 8090)

# 创建 builder (首次)
docker buildx create --name yunxingcloud-builder --use 2>/dev/null || docker buildx use yunxingcloud-builder

for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    port="${PORTS[$i]}"
    image="$REGISTRY/yunxingcloud-$svc:$TAG"

    echo "========================================"
    echo " Building $image ($PLATFORMS)"
    echo "========================================"

    if [ "${1:-}" = "push" ]; then
        docker buildx build --platform "$PLATFORMS" \
            --build-arg SERVICE="yunxingcloud-$svc" \
            --build-arg PORT="$port" \
            -t "$image" \
            --push .
    else
        docker buildx build --platform "$PLATFORMS" \
            --build-arg SERVICE="yunxingcloud-$svc" \
            --build-arg PORT="$port" \
            -t "$image" \
            --load .
    fi

    echo " ✓ $image"
done

echo ""
echo "全部完成: ${#SERVICES[@]} 镜像 × 2 架构"
docker buildx imagetools inspect "$REGISTRY/yunxingcloud-core:$TAG" 2>/dev/null | grep -E "Platform|Architecture" || true