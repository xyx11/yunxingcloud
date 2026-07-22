#!/bin/bash
# ============================================
# yunxingcloud K8s 一键部署脚本
# 用法: bash k8s/deploy.sh <your-domain.com>
# ============================================
set -euo pipefail

RED='\033[0;31m' GREEN='\033[0;32m' YELLOW='\033[1;33m' NC='\033[0m'

DOMAIN="${1:-}"
if [ -z "$DOMAIN" ]; then
  echo -e "${RED}用法: bash k8s/deploy.sh <your-domain.com>${NC}"
  echo "示例: bash k8s/deploy.sh yunxingcloud.com"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " yunxingcloud K8s Deploy"
echo " Domain: ${DOMAIN}"
echo "=========================================="

# ---- Step 1: 生成 Secrets ----
echo -e "\n${YELLOW}[1/6] 生成 Secrets...${NC}"
bash "$PROJECT_DIR/scripts/generate-secrets.sh" yunxingcloud

# ---- Step 2: 替换域名占位 ----
echo -e "\n${YELLOW}[2/6] 替换域名: ${DOMAIN}...${NC}"
cp "$SCRIPT_DIR/ingress.yaml" "$SCRIPT_DIR/ingress.yaml.bak"
if [[ "$(uname)" == "Darwin" ]]; then
  sed -i '' "s/{{DOMAIN}}/${DOMAIN}/g" "$SCRIPT_DIR/ingress.yaml"
else
  sed -i "s/{{DOMAIN}}/${DOMAIN}/g" "$SCRIPT_DIR/ingress.yaml"
fi

# ---- Step 3: 创建 Namespace ----
echo -e "\n${YELLOW}[3/6] 创建 Namespace...${NC}"
kubectl apply -f "$SCRIPT_DIR/namespace.yaml"

# ---- Step 4: 部署基础设施 ----
echo -e "\n${YELLOW}[4/6] 部署基础设施 (Redis/Nacos/ES)...${NC}"
kubectl apply -f "$SCRIPT_DIR/infrastructure.yaml" -n yunxingcloud

echo "等待基础设施就绪..."
kubectl wait --for=condition=ready pod -l app=redis -n yunxingcloud --timeout=120s 2>/dev/null || echo "  redis 可能尚未就绪，继续..."
kubectl wait --for=condition=ready pod -l app=nacos -n yunxingcloud --timeout=120s 2>/dev/null || echo "  nacos 可能尚未就绪，继续..."

# ---- Step 5: 部署应用 ----
echo -e "\n${YELLOW}[5/6] 部署 ConfigMap/Secret/Services/Deployments...${NC}"
kubectl apply -k "$SCRIPT_DIR" -n yunxingcloud

# ---- Step 6: 等待就绪 ----
echo -e "\n${YELLOW}[6/6] 等待应用就绪...${NC}"
kubectl wait --for=condition=ready pod -l app=yunxingcloud-gateway -n yunxingcloud --timeout=180s 2>/dev/null || echo "  等待超时，请手动检查: kubectl get pods -n yunxingcloud"

# ---- 结果 ----
echo ""
echo -e "${GREEN}=========================================="
echo " 部署完成！"
echo "==========================================${NC}"
echo ""
echo "访问地址:"
echo "  API:    https://api.${DOMAIN}"
echo "  Admin:  https://admin.${DOMAIN}"
echo ""
echo "检查状态:"
echo "  kubectl get pods -n yunxingcloud"
echo "  kubectl get ingress -n yunxingcloud"
echo ""
echo "TLS 证书 (如未配置):"
echo "  kubectl create secret tls yunxingcloud-tls --cert=cert.pem --key=key.pem -n yunxingcloud"
echo ""
echo "域名已保存到 ingress.yaml，备份在 ingress.yaml.bak"
