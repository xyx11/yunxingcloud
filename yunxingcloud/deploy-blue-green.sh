#!/bin/bash
set -e
# ============================================
# yunxingcloud 蓝绿部署脚本
# 维护 blue/green 两套环境，零停机切换
# ============================================

source deploy.conf 2>/dev/null || { echo "请先配置 deploy.conf"; exit 1; }

ENV="${1:-auto}"  # blue | green | auto | switch
NGX_DIR="/etc/nginx/conf.d"
BLUE_PORTS=(8080 8081 8083 8084 8085 8090)
GREEN_PORTS=(9080 9081 9083 9084 9085 9090)
SERVICES=(core usercenter payment order inventory gateway)

ssh_exec() { ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_HOST" "$@"; }
info() { echo -e "\033[0;32m[INFO]\033[0m  $1"; }

# ---- 检测当前活跃环境 ----
current_env() {
    ssh_exec "grep -q '8080' /etc/nginx/conf.d/yunxingcloud-upstream.conf 2>/dev/null && echo blue || echo green" 2>/dev/null || echo blue
}

# ---- 获取目标端口列表 ----
get_ports() {
    local env=$1
    if [ "$env" = "green" ]; then
        echo "${GREEN_PORTS[@]}"
    else
        echo "${BLUE_PORTS[@]}"
    fi
}

# ---- 部署到非活跃环境 ----
deploy() {
    local active=$(current_env)
    local target="green"
    [ "$active" = "green" ] && target="blue"

    info "当前活跃: $active → 部署到: $target"

    local ports=($(get_ports "$target"))

    for i in "${!SERVICES[@]}"; do
        local svc="${SERVICES[$i]}"
        local port="${ports[$i]}"
        info "启动 $svc 端口 $port..."
        ssh_exec "
            nohup java -jar $DEPLOY_DIR/app/yunxingcloud-$svc.jar \
                --server.port=$port \
                > $DEPLOY_DIR/logs/$svc-$target.log 2>&1 &
        "
        sleep 4
    done
    sleep 10

    info "健康检查 $target 环境..."
    for i in "${!SERVICES[@]}"; do
        local svc="${SERVICES[$i]}"
        local port="${ports[$i]}"
        ssh_exec "curl -sf http://127.0.0.1:$port/actuator/health" || {
            echo "服务 $svc:$port 不健康"
            exit 1
        }
    done
    info "$target 环境就绪"
}

# ---- 切换流量 ----
switch() {
    local active=$(current_env)
    local target="green"
    [ "$active" = "green" ] && target="blue"

    local active_port
    local target_port
    if [ "$active" = "blue" ]; then
        active_port=8080; target_port=9080
    else
        active_port=9080; target_port=8080
    fi

    info "切换流量: $active($active_port) → $target($target_port)"
    ssh_exec "
        sed -i 's/127.0.0.1:$active_port/127.0.0.1:$target_port/g' $NGX_DIR/yunxingcloud-upstream.conf
        nginx -t && nginx -s reload
    "
    info "切换完成 → $target"
}

# ---- 停止旧环境 ----
stop_idle() {
    local active=$(current_env)
    local idle="blue"
    [ "$active" = "blue" ] && idle="green"

    info "停止闲置环境: $idle"
    local ports=($(get_ports "$idle"))
    for port in "${ports[@]}"; do
        ssh_exec "lsof -ti :$port | xargs kill -9 2>/dev/null || true"
    done
}

# ============================================
echo "蓝绿部署: ${ENV}"
case "$ENV" in
    deploy) deploy ;;
    switch) switch ;;
    auto)   deploy && switch && sleep 5 && stop_idle ;;
    status) echo "当前活跃: $(current_env)" ;;
    *)      echo "用法: $0 {deploy|switch|auto|status}" ;;
esac