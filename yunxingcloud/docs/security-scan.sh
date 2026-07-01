#!/bin/bash
set -e
# ============================================
# OWASP ZAP 安全扫描脚本
# 用法:
#   ./docs/security-scan.sh quick    # 快速扫描 (爬虫)
#   ./docs/security-scan.sh full      # 全量扫描 (爬虫+主动)
#   ./docs/security-scan.sh api       # API 端点扫描
# ============================================

TARGET="${TARGET_URL:-http://localhost:8090}"
ZAP_HOST="${ZAP_HOST:-localhost}"
ZAP_PORT="${ZAP_PORT:-8080}"
ZAP_API_KEY="${ZAP_API_KEY:-}"
REPORT_DIR="docs/security-reports"
MODE="${1:-quick}"

mkdir -p "$REPORT_DIR"

# 检查 ZAP 是否运行
check_zap() {
    curl -sf "http://${ZAP_HOST}:${ZAP_PORT}/JSON/core/view/version/" > /dev/null 2>&1 || {
        echo "ZAP 未运行。启动方式:"
        echo "  docker run -d --name zap -p 8080:8080 -i ghcr.io/zaproxy/zaproxy:stable zap.sh -daemon -host 0.0.0.0 -port 8080"
        exit 1
    }
}

zap_api() {
    local endpoint=$1; shift
    local params=""
    [ -n "$ZAP_API_KEY" ] && params="?apikey=$ZAP_API_KEY"
    curl -s "http://${ZAP_HOST}:${ZAP_PORT}/JSON/${endpoint}/${params}" "$@"
}

quick_scan() {
    echo "[1/4] 爬虫扫描 $TARGET ..."
    zap_api "spider/action/scan/" -d "url=$TARGET"
    sleep 10

    echo "[2/4] 等待爬虫完成..."
    while true; do
        progress=$(zap_api "spider/view/status/")
        if echo "$progress" | grep -q '"100"'; then break; fi
        sleep 2
    done

    echo "[3/4] 生成报告..."
    zap_api "core/other/htmlreport/" -o "$REPORT_DIR/zap-quick-$(date +%Y%m%d_%H%M%S).html"
    echo "[4/4] 完成"
}

full_scan() {
    echo "[1/5] 爬虫扫描..."
    quick_scan

    echo "[2/5] 主动扫描..."
    zap_api "ascan/action/scan/" -d "url=$TARGET"
    echo "[3/5] 等待主动扫描完成 (可能需要几分钟)..."
    while true; do
        progress=$(zap_api "ascan/view/status/")
        if echo "$progress" | grep -q '"100"'; then break; fi
        sleep 5
    done

    echo "[4/5] 生成完整报告..."
    zap_api "core/other/htmlreport/" -o "$REPORT_DIR/zap-full-$(date +%Y%m%d_%H%M%S).html"
    zap_api "core/other/jsonreport/" -o "$REPORT_DIR/zap-full-$(date +%Y%m%d_%H%M%S).json"
    echo "[5/5] 完成"
}

api_scan() {
    echo "[1/3] 导入 OpenAPI 定义..."
    zap_api "openapi/action/importUrl/" -d "url=$TARGET/v3/api-docs"
    sleep 5

    echo "[2/3] 主动扫描 API 端点..."
    zap_api "ascan/action/scan/" -d "url=$TARGET"
    sleep 30

    echo "[3/3] 生成 API 安全报告..."
    zap_api "core/other/htmlreport/" -o "$REPORT_DIR/zap-api-$(date +%Y%m%d_%H%M%S).html"
    echo "完成"
}

check_zap

case "$MODE" in
    quick) quick_scan ;;
    full)  full_scan ;;
    api)   api_scan ;;
    *)
        echo "用法: $0 {quick|full|api}"
        echo "  quick - 快速爬虫扫描"
        echo "  full  - 全量扫描（爬虫+主动攻击）"
        echo "  api   - API 端点主动扫描（需 OpenAPI）"
        ;;
esac