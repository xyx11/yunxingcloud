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
SERVICES="core usercenter payment order inventory gateway"

ssh_exec() { ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_HOST" "$@"; }
info() { echo -e "\033[0;32m[INFO]\033[0m  $1"; }

# ---- 检测当前活跃环境 ----
current_env() {
    ssh_exec "grep -q 'upstream yunxingcloud_backend' /etc/nginx/nginx.conf && \
              grep '127.0.0.1:80[0-9]*' /etc/nginx/nginx.conf | head -1 | grep -q '9080' && \
              echo green || echo blue" 2>/dev/null || echo blue
}

# ---- 部署到非活跃环境 ----
deploy() {
    local active=$(current_env)
    local target="green"
    [ "$active" = "green" ] && target="blue"

    local ports_ref="BLUE_PORTS[@]"
    [ "$target" = "green" ] && ports_ref="GREEN_PORTS[@]"

    info "当前活跃: $active → 部署到: $target"

    # 启动目标环境
    local i=0
    for svc in $SERVICES; do
        local port=${!ports_ref:$i:1}
        ssh_exec "
            nohup java -jar $DEPLOY_DIR/app/yunxingcloud-$svc.jar \
                --server.port=$port \
                > $DEPLOY_DIR/logs/$svc-$target.log 2>&1 &
        "
        ((i++))
        sleep 4
    done
    sleep 10

    # 健康检查
    info "健康检查 $target 环境..."
    i=0
    for svc in $SERVICES; do
        local port=${!ports_ref:$i:1}
        ssh_exec "curl -sf http://127.0.0.1:$port/actuator/health" || {
            echo "服务 $svc:$port 不健康"
            exit 1
        }
        ((i++))
    done
    info "$target 环境就绪"
}

# ---- 切换流量 ----
switch() {
    local active=$(current_env)
    local target="green"
    [ "$active" = "green" ] && target="blue"

    info "切换流量: $active → $target"
    ssh_exec "
        sed -i 's/127.0.0.1:${active}_port/127.0.0.1:${target}_port/g' $NGX_DIR/yunxingcloud-upstream.conf
        nginx -t && nginx -s reload
    "
    info "切换完成 → $target"
}

# ---- 停止旧环境 ----
stop_idle() {
    local active=$(current_env)
    local idle="blue"
    [ "$active" = "blue" ] && idle="green"
    local ports_ref="GREEN_PORTS[@]"
    [ "$idle" = "green" ] && ports_ref="GREEN_PORTS[@]"

    info "停止闲置环境: $idle"
    for port in ${!ports_ref}; do
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