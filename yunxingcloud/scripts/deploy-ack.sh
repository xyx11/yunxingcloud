#!/bin/bash
# ============================================
# yunxingcloud 阿里云 ACK 生产部署脚本
# 用法: bash scripts/deploy-ack.sh <ACR_REGISTRY> <VERSION>
# 示例: bash scripts/deploy-ack.sh registry.cn-hangzhou.aliyuncs.com/yunxingcloud v1.0.0
# ============================================
set -euo pipefail

RED='\033[0;31m' GREEN='\033[0;32m' YELLOW='\033[1;33m' NC='\033[0m'

ACR_REGISTRY="${1:-}"
VERSION="${2:-latest}"

if [ -z "$ACR_REGISTRY" ]; then
  echo -e "${RED}用法: bash scripts/deploy-ack.sh <ACR_REGISTRY> [VERSION]${NC}"
  echo "示例: bash scripts/deploy-ack.sh registry.cn-hangzhou.aliyuncs.com/yunxingcloud v1.0.0"
  echo ""
  echo "前置条件:"
  echo "  1. 已创建 ACK 集群并配置 kubectl"
  echo "  2. 已创建 ACR 镜像仓库并 docker login"
  echo "  3. 已配置 RDS MySQL + Redis (或使用集群内部署)"
  echo "  4. 已配置域名 DNS 解析到 SLB IP"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " yunxingcloud ACK Deploy"
echo " Registry: ${ACR_REGISTRY}"
echo " Version:  ${VERSION}"
echo "=========================================="

# Step 1: 构建 + 推送镜像
echo -e "\n${YELLOW}[1/4] 构建 & 推送镜像到 ACR...${NC}"
./mvnw package -pl yunxingcloud-core,yunxingcloud-usercenter,yunxingcloud-gateway,yunxingcloud-payment,yunxingcloud-order,yunxingcloud-inventory -DskipTests -q --no-transfer-progress

for svc in core usercenter gateway payment order inventory; do
  case $svc in core) PORT=8080;; usercenter) PORT=8081;; gateway) PORT=8090;;
    payment) PORT=8083;; order) PORT=8084;; inventory) PORT=8085;; esac
  JAR="yunxingcloud-${svc}/target/yunxingcloud-${svc}-0.0.1-SNAPSHOT.jar"

  echo "  Building yunxingcloud-${svc}..."
  docker build --build-arg JAR_FILE="$JAR" --build-arg PORT=$PORT \
    -t ${ACR_REGISTRY}/yunxingcloud-${svc}:${VERSION} \
    -f Dockerfile.local .

  echo "  Pushing yunxingcloud-${svc}..."
  docker push ${ACR_REGISTRY}/yunxingcloud-${svc}:${VERSION}
done

# Step 2: 生成 Secrets
echo -e "\n${YELLOW}[2/4] 生成 Secrets...${NC}"
bash "$PROJECT_DIR/scripts/generate-secrets.sh" yunxingcloud

# Step 3: 更新 kustomize 中的镜像 tag
echo -e "\n${YELLOW}[3/4] 更新镜像版本: ${VERSION}...${NC}"
OVERLAY_DIR="$PROJECT_DIR/k8s/overlays/ack"
if [[ "$(uname)" == "Darwin" ]]; then
  sed -i '' "s/newTag:.*/newTag: ${VERSION}/g" "$OVERLAY_DIR/kustomization.yaml"
  sed -i '' "s|newName:.*yunxingcloud-|newName: ${ACR_REGISTRY}/yunxingcloud-|g" "$OVERLAY_DIR/kustomization.yaml"
else
  sed -i "s/newTag:.*/newTag: ${VERSION}/g" "$OVERLAY_DIR/kustomization.yaml"
  sed -i "s|newName:.*yunxingcloud-|newName: ${ACR_REGISTRY}/yunxingcloud-|g" "$OVERLAY_DIR/kustomization.yaml"
fi

# Step 4: 部署到 ACK
echo -e "\n${YELLOW}[4/4] 部署到 ACK...${NC}"
kubectl apply -k "$OVERLAY_DIR"

echo ""
echo -e "${GREEN}=========================================="
echo " 部署完成！"
echo "==========================================${NC}"
echo ""
echo "检查状态:"
echo "  kubectl get pods -n yunxingcloud -w"
echo "  kubectl get ingress -n yunxingcloud"
echo ""
echo "获取 SLB 公网 IP:"
echo "  kubectl get svc -n kube-system nginx-ingress-lb -o jsonpath='{.status.loadBalancer.ingress[0].ip}'"
