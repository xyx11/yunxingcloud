#!/bin/bash
set -e
# ============================================
# yunxingcloud 一键演示脚本
# 启动后端 → 等待就绪 → 打开浏览器 → 冒烟测试
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

GREEN='\033[0;32m'; BLUE='\033[0;34m'; NC='\033[0m'
info() { echo -e "${GREEN}[INFO]${NC} $1"; }

info "构建项目..."
./mvnw compile -pl yunxingcloud-core -q 2>/dev/null || true
cd frontend && npx vite build 2>/dev/null || true && cd ..

info "启动 yunxingcloud-core (端口 8080)..."
./mvnw spring-boot:run -pl yunxingcloud-core -Dspring-boot.run.profiles=dev > /tmp/yunxingcloud-demo.log 2>&1 &
PID=$!

info "等待服务就绪..."
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
  [ "$code" = "200" ] && break
  sleep 1
done
echo ""

info "冒烟测试..."
# 1. 健康检查
echo -n "  健康检查: "; curl -s http://localhost:8080/actuator/health | python3 -c "import sys,json; print(json.load(sys.stdin)['status'])"
# 2. 登录
TOKEN=$(curl -s -X POST http://localhost:8080/api/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo -n "  登录测试: "; [ -n "$TOKEN" ] && echo "PASS" || echo "FAIL"
# 3. 用户信息
echo -n "  用户信息: "; curl -s http://localhost:8080/api/user -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'{d[\"username\"]} ({len(d[\"authorities\"])} perms)')"
# 4. 菜单
echo -n "  菜单树:   "; COUNT=$(curl -s http://localhost:8080/api/menus/tree -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))"); echo "$COUNT 个菜单"
# 5. Dashboard
echo -n "  仪表盘:   "; curl -s http://localhost:8080/api/stats/dashboard -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'用户{d[\"userCount\"]} 日志{d[\"operLogCount\"]}')"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}  演示就绪! 访问 http://localhost:8080${NC}"
echo -e "${BLUE}  账号: admin / admin123${NC}"
echo -e "${BLUE}  文档: http://localhost:8080/doc.html${NC}"
echo -e "${BLUE}============================================${NC}"

open http://localhost:8080
wait $PID
