#!/bin/bash
# yunxingcloud 健康检查脚本 (cron: */1 * * * *)

RED='\033[0;31m'; GREEN='\033[0;32m'; NC='\033[0m'
FAIL=0

check() { local p=$1 n=$2; if curl -sf --max-time 3 http://127.0.0.1:$p/actuator/health >/dev/null 2>&1; then echo -e "  ${GREEN}✓${NC} $n :$p"; else echo -e "  ${RED}✗${NC} $n :$p — 重启中..."; systemctl restart yunxingcloud-$n 2>/dev/null; FAIL=1; fi }

echo "=== yunxingcloud Health Check $(date +%H:%M:%S) ==="
check 8080 core
check 8081 usercenter
check 8083 payment
check 8084 order
check 8085 inventory
check 8090 gateway

[ $FAIL -eq 0 ] && echo "✓ All 6 services healthy" || echo "⚠ Some services restarted"
exit $FAIL