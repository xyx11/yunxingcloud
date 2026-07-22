#!/bin/bash
# yunxingcloud 健康检查 (cron: */2 * * * *)
# 用法: ./health-check.sh [--alert]

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
FAIL=0; WARN=0

check_http() { local p=$1 n=$2
  if curl -sf --max-time 5 http://127.0.0.1:$p/actuator/health >/dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} $n :$p"
  else
    echo -e "  ${RED}✗${NC} $n :$p — 重启中..."
    systemctl restart yunxingcloud-$n 2>/dev/null; FAIL=1
  fi
}

check_port() { local p=$1 n=$2
  if timeout 2 bash -c "echo >/dev/tcp/127.0.0.1/$p" 2>/dev/null; then
    echo -e "  ${GREEN}✓${NC} $n :$p"
  else
    echo -e "  ${RED}✗${NC} $n :$p"; FAIL=1
  fi
}

echo "=== yunxingcloud Health $(date '+%Y-%m-%d %H:%M:%S') ==="

# Microservices
echo "[Services]"
check_http 8080 core
check_http 8081 usercenter
check_http 8083 payment
check_http 8084 order
check_http 8085 inventory
check_http 8090 gateway

# Infrastructure
echo "[Infrastructure]"
check_port 3306 MySQL
check_port 6379 Redis
check_port 8848 Nacos

# Disk
echo "[Disk]"
USAGE=$(df / | tail -1 | awk '{print $5}' | tr -d '%')
if [ "$USAGE" -gt 85 ]; then echo -e "  ${RED}✗${NC} Disk ${USAGE}%"; WARN=1
elif [ "$USAGE" -gt 70 ]; then echo -e "  ${YELLOW}⚠${NC} Disk ${USAGE}%"; WARN=1
else echo -e "  ${GREEN}✓${NC} Disk ${USAGE}%"
fi

# Memory
MEM=$(free | grep Mem | awk '{printf "%d", $3/$2*100}')
if [ "$MEM" -gt 90 ]; then echo -e "  ${RED}✗${NC} Memory ${MEM}%"; WARN=1
elif [ "$MEM" -gt 80 ]; then echo -e "  ${YELLOW}⚠${NC} Memory ${MEM}%"; WARN=1
else echo -e "  ${GREEN}✓${NC} Memory ${MEM}%"
fi

# Summary
echo ""
[ $FAIL -eq 0 ] && [ $WARN -eq 0 ] && echo -e "${GREEN}✓ All systems healthy${NC}" && exit 0
[ $FAIL -gt 0 ] && echo -e "${RED}✗ $FAIL service(s) restarted${NC}"
[ $WARN -gt 0 ] && echo -e "${YELLOW}⚠ $WARN warning(s)${NC}"
exit 1
