#!/bin/bash
# ============================================
# yunxingcloud 灰度发布脚本
# 用法:
#   ./deploy-canary.sh start <service> <version>   # 启动金丝雀实例
#   ./deploy-canary.sh promote <service>             # 全量切换
#   ./deploy-canary.sh abort <service>               # 回滚金丝雀
#   ./deploy-canary.sh status <service>              # 查看状态
# ============================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/deploy.conf" 2>/dev/null || { echo "请先配置 deploy.conf"; exit 1; }

CANARY_WEIGHT=${CANARY_WEIGHT:-10}   # 金丝雀流量百分比
CANARY_PORT_OFFSET=100               # 金丝雀端口偏移

SERVICES=("core" "gateway" "usercenter" "payment" "order" "inventory")
SERVICE_PORTS=(8080 8090 8081 8083 8084 8085)

info()  { echo -e "\033[0;32m[INFO]\033[0m  $1"; }
warn()  { echo -e "\033[1;33m[WARN]\033[0m  $1"; }
error() { echo -e "\033[0;31m[ERROR]\033[0m $1"; exit 1; }

ssh_exec() { ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_HOST" "$@"; }

# 获取服务端口
get_port() {
    for i in "${!SERVICES[@]}"; do
        if [ "${SERVICES[$i]}" = "$1" ]; then echo "${SERVICE_PORTS[$i]}"; return 0; fi
    done
    error "未知服务: $1"
}

get_canary_port() { echo $(($(get_port "$1") + CANARY_PORT_OFFSET)); }

# ---- 启动金丝雀 ----
start_canary() {
    local svc=$1 ver=$2
    local port=$(get_port "$svc")
    local canary_port=$(get_canary_port "$svc")

    info "启动 $svc 金丝雀实例 v$ver (端口 $canary_port)..."

    ssh_exec "
        # 启动金丝雀实例 (新版本 JAR)
        CANARY_JAR=\$(ls $DEPLOY_DIR/app/yunxingcloud-$svc-*.jar 2>/dev/null | head -1)
        [ -z \"\$CANARY_JAR\" ] && CANARY_JAR=$DEPLOY_DIR/app/yunxingcloud-$svc.jar
        nohup java -jar \$CANARY_JAR \
            --server.port=$canary_port \
            --spring.application.name=yunxingcloud-$svc-canary \
            > $DEPLOY_DIR/logs/$svc-canary.log 2>&1 &
        sleep 5

        # 健康检查
        for i in \$(seq 1 12); do
            code=\$(curl -s -o /dev/null -w '%{http_code}' http://localhost:$canary_port/actuator/health 2>/dev/null || echo 0)
            if [ \"\$code\" = \"200\" ]; then
                echo '金丝雀实例健康 (HTTP 200)'
                exit 0
            fi
            sleep 2
        done
        echo '金丝雀实例启动超时' && exit 1
    "

    # 更新 Nginx 分流: 10% 流量 → 金丝雀
    info "更新 Nginx 分流 (金丝雀 ${CANARY_WEIGHT}%)..."
    local stable_weight=$((100 - CANARY_WEIGHT))
    ssh_exec "
        cat > /etc/nginx/conf.d/yunxingcloud-canary.conf << NGX
upstream yunxingcloud_${svc}_canary {
    server 127.0.0.1:${port} weight=${stable_weight};
    server 127.0.0.1:${canary_port} weight=${CANARY_WEIGHT};
}
NGX
        nginx -t && nginx -s reload
    "

    info "金丝雀部署完成: $svc v$ver (${CANARY_WEIGHT}% 流量)"
}

# ---- 全量切换 ----
promote_canary() {
    local svc=$1
    local port=$(get_port "$svc")
    local canary_port=$(get_canary_port "$svc")

    info "全量切换 $svc → 金丝雀版本..."

    ssh_exec "
        # 停止旧实例
        systemctl stop yunxingcloud-$svc 2>/dev/null || true
        sleep 2

        # 金丝雀接管主端口
        kill \$(lsof -ti :$canary_port) 2>/dev/null || true
        sleep 2
        JAR=\$(ls $DEPLOY_DIR/app/yunxingcloud-$svc*.jar 2>/dev/null | head -1)
        nohup java -jar \$JAR \
            --server.port=$port \
            > $DEPLOY_DIR/logs/$svc.log 2>&1 &

        # 移除金丝雀分流配置
        rm -f /etc/nginx/conf.d/yunxingcloud-canary.conf
        nginx -t && nginx -s reload
    "

    info "全量切换完成: $svc"
}

# ---- 回滚金丝雀 ----
abort_canary() {
    local svc=$1
    local canary_port=$(get_canary_port "$svc")

    warn "回滚 $svc 金丝雀实例..."

    ssh_exec "
        kill \$(lsof -ti :$canary_port) 2>/dev/null || true
        rm -f /etc/nginx/conf.d/yunxingcloud-canary.conf
        nginx -t && nginx -s reload
    "

    info "金丝雀已回滚: $svc"
}

# ---- 状态 ----
status_canary() {
    local svc=$1
    local port=$(get_port "$svc")
    local canary_port=$(get_canary_port "$svc")

    ssh_exec "
        echo '=== $svc ==='
        echo -n '主实例 (:$port): '
        curl -s -o /dev/null -w '%{http_code}' http://localhost:$port/actuator/health 2>/dev/null || echo 'DOWN'
        echo -n '金丝雀 (:$canary_port): '
        curl -s -o /dev/null -w '%{http_code}' http://localhost:$canary_port/actuator/health 2>/dev/null || echo 'N/A'
    "
}

# ============================================
CMD=${1:-status}
case "$CMD" in
    start)   start_canary "$2" "$3" ;;
    promote) promote_canary "$2" ;;
    abort)   abort_canary "$2" ;;
    status)  status_canary "${2:-core}" ;;
    *)
        echo "用法: $0 {start|promote|abort|status} <service> [version]"
        echo ""
        echo "  start <svc> <ver>  - 启动金丝雀实例 (${CANARY_WEIGHT}% 流量)"
        echo "  promote <svc>       - 全量切换到金丝雀版本"
        echo "  abort <svc>         - 回滚金丝雀实例"
        echo "  status <svc>        - 查看灰度状态"
        echo ""
        echo "  服务: core | gateway | usercenter | payment | order | inventory"
        exit 1
        ;;
esac