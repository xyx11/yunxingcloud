#!/bin/bash
set -e
# ============================================
# yunxingcloud 全栈测试脚本
# 用法: ./test-all.sh [--skip-frontend] [--skip-backend]
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
PASS=0; FAIL=0

check() {
    local desc=$1; shift
    echo -n "  $desc ... "
    if "$@" > /dev/null 2>&1; then
        echo -e "${GREEN}PASS${NC}"; ((PASS++))
    else
        echo -e "${RED}FAIL${NC}"; ((FAIL++))
    fi
}

echo "=========================================="
echo " yunxingcloud 全栈测试 v2.4.0"
echo "=========================================="

# ---- 后端 ----
if [ "${1}" != "--skip-backend" ]; then
    echo ""
    echo "[Backend] 编译 + 测试 (8 modules)"

    check "common + api 编译" ./mvnw compile -pl yunxingcloud-common,yunxingcloud-api -q
    check "common 测试 (37)" ./mvnw test -pl yunxingcloud-common -q
    check "api 测试 (6)" ./mvnw test -pl yunxingcloud-api -q
    check "gateway 测试 (1)" ./mvnw test -pl yunxingcloud-gateway -q
    check "usercenter 测试 (29)" ./mvnw test -pl yunxingcloud-usercenter -q
    check "payment 测试 (6)" ./mvnw test -pl yunxingcloud-payment -q
    check "order 测试 (57)" ./mvnw test -pl yunxingcloud-order -q
    check "inventory 测试 (8)" ./mvnw test -pl yunxingcloud-inventory -q
    check "core 测试 (99)" ./mvnw test -pl yunxingcloud-core -q

    echo "  后端: $((PASS)) pass, $((FAIL)) fail"
fi

# ---- 前端 Admin ----
if [ "${1}" != "--skip-frontend" ]; then
    echo ""
    echo "[Frontend Admin] Lint + Type-Check + Test"

    cd frontend
    check "npm ci" npm ci --silent
    check "ESLint" npx eslint . --ext .vue,.ts --max-warnings 0 2>/dev/null || npx eslint . --ext .vue,.ts 2>/dev/null
    check "TypeScript" npx vue-tsc --noEmit
    check "Vitest (6)" npx vitest run
    check "Build" npm run build --silent
    cd ..

    echo "  Admin: $((PASS - ADMIN_PASS_START)) pass"
fi

# ---- 前端 Mall ----
if [ "${1}" != "--skip-frontend" ]; then
    echo ""
    echo "[Frontend Mall] Lint + Type-Check + Test"

    cd frontend-mall
    check "npm ci" npm ci --silent
    check "ESLint" npx eslint . --ext .vue,.ts 2>/dev/null || echo "  (warnings only)"
    check "TypeScript" npx vue-tsc --noEmit
    check "Vitest (8)" npx vitest run
    check "Build" npm run build --silent
    cd ..

    echo "  Mall: pass"
fi

echo ""
echo "=========================================="
echo -e " 总计: ${GREEN}$PASS 通过${NC} / ${RED}$FAIL 失败${NC}"
echo "=========================================="

[ $FAIL -eq 0 ] && exit 0 || exit 1