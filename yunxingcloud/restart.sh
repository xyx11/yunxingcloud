#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

RED='\033[0;31m'; GREEN='\033[0;32m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }

# ---- 端口 ----
PORTS=(8081 8083 8084 8085 8080 8090)
SERVICES=("usercenter" "payment" "order" "inventory" "core" "gateway")
JARS=("yunxingcloud-usercenter/target/yunxingcloud-usercenter-[0-9]*.jar"
      "yunxingcloud-payment/target/yunxingcloud-payment-[0-9]*.jar"
      "yunxingcloud-order/target/yunxingcloud-order-[0-9]*.jar"
      "yunxingcloud-inventory/target/yunxingcloud-inventory-[0-9]*.jar"
      "yunxingcloud-core/target/yunxingcloud-core-[0-9]*.jar"
      "yunxingcloud-gateway/target/yunxingcloud-gateway-[0-9]*.jar")

echo "========== 重启 yunxingcloud (6 微服务) =========="

# 1. 停止
echo "1. 停止旧服务..."
for p in "${PORTS[@]}"; do
    lsof -ti :"$p" | xargs kill -9 2>/dev/null || true
done
sleep 2

# 2. 构建前端
echo "2. 构建前端 (admin)..."
cd "$ROOT/frontend" && npx vite build 2>&1 | tail -1

# 3. 构建全部后端模块
echo "3. 构建后端 (6 modules)..."
cd "$ROOT" && ./mvnw clean package -DskipTests -q 2>&1 | tail -1
info "构建完成"

# 4. 启动服务（按依赖顺序）
for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    port="${PORTS[$i]}"
    jar="${JARS[$i]}"
    echo "4. 启动 $svc (端口 $port)..."
    nohup java -jar "$ROOT/$jar" > "/tmp/yunxingcloud-$svc.log" 2>&1 &
    sleep 4
done
sleep 6

# 5. 验证
echo ""
echo "========== 验证 =========="
for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    port="${PORTS[$i]}"
    code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:$port/actuator/health" 2>/dev/null || echo "N/A")
    case "$code" in
        200) echo -e "  $svc ($port): ${GREEN}HTTP $code${NC}" ;;
        *)   echo -e "  $svc ($port): ${RED}$code${NC}" ;;
    esac
done

echo ""
echo "  Admin:  http://localhost:5173"
echo "  Gateway: http://localhost:8090"
echo "  admin / admin123"