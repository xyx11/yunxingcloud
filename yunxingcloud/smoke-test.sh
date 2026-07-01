#!/bin/bash
set -e
# ============================================
# yunxingcloud 冒烟测试脚本
# 验证全部 6 服务健康 + 关键 API
# ============================================

BASE="${BASE_URL:-http://localhost:8090}"
PASS=0; FAIL=0
RED='\033[0;31m'; GREEN='\033[0;32m'; NC='\033[0m'

check() {
    local desc=$1 method=$2 url=$3 expected=$4 data=$5
    local code
    if [ -n "$data" ]; then
        code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$BASE$url" \
            -H "Content-Type: application/json" ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
            -d "$data" 2>/dev/null || echo "000")
    else
        code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$BASE$url" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"} 2>/dev/null || echo "000")
    fi
    if [ "$code" = "$expected" ]; then
        echo -e "  ${GREEN}✓${NC} $desc (HTTP $code)"
        ((PASS++))
    else
        echo -e "  ${RED}✗${NC} $desc (expected $expected, got $code)"
        ((FAIL++))
    fi
}

echo "=========================================="
echo " yunxingcloud Smoke Test"
echo " BASE: $BASE"
echo "=========================================="

# ---- 1. 基础设施 ----
echo ""
echo "[1] 健康检查"
check "Core Health"     GET "/actuator/health" "200"
check "Gateway Health"  GET "http://localhost:8090/actuator/health" "200"

# ---- 2. 公开端点 ----
echo ""
echo "[2] 公开端点"
check "登录页"          GET "/" "200"
check "API 文档"       GET "/doc.html" "200"
check "商品列表(公开)"  GET "/api/products" "200"

# ---- 3. 认证 ----
echo ""
echo "[3] 认证"
check "登录成功"        POST "/api/login" "200" \
    '{"username":"admin","password":"admin123"}'

TOKEN=$(curl -s -X POST "$BASE/api/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}' | \
    python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
    echo -e "  ${RED}✗${NC} 无法获取 Token，跳过认证测试"
else
    check "错误密码"    POST "/api/login" "401" \
        '{"username":"admin","password":"wrong"}'

    # ---- 4. 管理端点 (需认证) ----
    echo ""
    echo "[4] 管理端点"
    check "工单列表"    GET "/api/tickets" "200"
    check "支付列表"    GET "/api/payment/orders" "200"
    check "订单列表"    GET "/api/orders" "200"
    check "库存列表"    GET "/api/inventory" "200"
    check "仓库列表"    GET "/api/warehouses" "200"

    # ---- 5. 写操作 ----
    echo ""
    echo "[5] 写操作"
    check "创建工单"    POST "/api/tickets" "200" \
        '{"title":"Smoke Test Ticket","type":"other","priority":"low"}'
    check "添加仓库"    POST "/api/warehouses" "200" \
        '{"name":"Smoke WH","address":"Test"}'
fi

echo ""
echo "=========================================="
echo -e " 结果: ${GREEN}$PASS 通过${NC} / ${RED}$FAIL 失败${NC}"
echo "=========================================="
[ $FAIL -eq 0 ] && exit 0 || exit 1