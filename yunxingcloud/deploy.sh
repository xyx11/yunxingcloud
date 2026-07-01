#!/bin/bash
set -e

# ============================================
# yunxingcloud 一键部署脚本
# 用法: ./deploy.sh [full|build|upload|start|restart|logs|status]
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

source deploy.conf 2>/dev/null || {
    echo "[ERROR] 请先配置 deploy.conf"; exit 1
}

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REMOTE_APP="$DEPLOY_DIR/app"
REMOTE_LOG="$DEPLOY_DIR/logs"

SERVICES="yunxingcloud-core yunxingcloud-gateway yunxingcloud-usercenter yunxingcloud-payment yunxingcloud-order yunxingcloud-inventory"

# ---- 颜色 ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ---- SSH helper ----
ssh_exec() {
    ssh -p "$SERVER_PORT" -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_HOST" "$@"
}

# ---- 构建 ----
build() {
    info "构建前端 Admin (Vite)..."
    cd frontend && npm run build --silent 2>/dev/null && cd ..
    info "构建前端 Mall (Vite)..."
    cd frontend-mall && npm run build --silent 2>/dev/null && cd ..
    info "构建后端 (Maven 全模块)..."
    ./mvnw clean package -DskipTests -q
    info "全部构建完成"
}

# ---- 打包 ----
package() {
    local PKG="yunxingcloud-$TIMESTAMP.tar.gz"
    info "打包: $PKG"
    tar -czf "/tmp/$PKG" \
        yunxingcloud-core/target/yunxingcloud-core-*.jar \
        yunxingcloud-gateway/target/yunxingcloud-gateway-*.jar \
        yunxingcloud-usercenter/target/yunxingcloud-usercenter-*.jar \
        yunxingcloud-payment/target/yunxingcloud-payment-*.jar \
        yunxingcloud-order/target/yunxingcloud-order-*.jar \
        yunxingcloud-inventory/target/yunxingcloud-inventory-*.jar \
        frontend-mall/dist/ \
        deploy.conf 2>/dev/null
    echo "/tmp/$PKG"
}

# ---- 上传 ----
upload() {
    local PKG=$(package)
    info "上传到 $SERVER_HOST ..."
    ssh_exec "mkdir -p $REMOTE_APP $REMOTE_LOG $DEPLOY_DIR/backup /opt/yunxingcloud/mall"
    scp -P "$SERVER_PORT" -o StrictHostKeyChecking=no "$PKG" "$SERVER_USER@$SERVER_HOST:$DEPLOY_DIR/"
    ssh_exec "cd $DEPLOY_DIR && tar -xzf $(basename $PKG) && rm $(basename $PKG)"
    # 部署商城前端静态文件
    ssh_exec "cp -r $DEPLOY_DIR/frontend-mall/dist/* /opt/yunxingcloud/mall/"
    info "上传完成"
}

# ---- 远程启动 ----
start() {
    info "启动所有微服务..."
    ssh_exec "
        for svc in yunxingcloud-usercenter yunxingcloud-payment yunxingcloud-order yunxingcloud-inventory yunxingcloud-core yunxingcloud-gateway; do
            systemctl start \$svc 2>/dev/null || true
        done
        sleep 5
        echo 'all started'
    "
}

# ---- 停止 ----
stop() {
    info "停止所有微服务..."
    ssh_exec "
        for svc in yunxingcloud-gateway yunxingcloud-core yunxingcloud-inventory yunxingcloud-order yunxingcloud-payment yunxingcloud-usercenter; do
            systemctl stop \$svc 2>/dev/null || true
        done
        echo 'all stopped'
    "
}

# ---- 重启 ----
restart() {
    info "重启所有微服务..."
    ssh_exec "
        for svc in yunxingcloud-usercenter yunxingcloud-payment yunxingcloud-order yunxingcloud-inventory yunxingcloud-core yunxingcloud-gateway; do
            systemctl restart \$svc 2>/dev/null || true
            sleep 3
        done
        echo 'done'
    "
}

# ---- 健康检查 ----
health_check() {
    info "健康检查 ..."
    sleep 5
    local ports=(8081 8083 8084 8085 8080 8090)
    local svcs=("usercenter" "payment" "order" "inventory" "core" "gateway")
    local all_up=true
    for i in "${!svcs[@]}"; do
        local svc="${svcs[$i]}"
        local port="${ports[$i]}"
        local code=$(ssh_exec "curl -s -o /dev/null -w '%{http_code}' http://localhost:$port/actuator/health" 2>/dev/null || echo "000")
        if [ "$code" = "200" ]; then
            info "  ${svc}:${port} UP"
        else
            warn "  ${svc}:${port} HTTP $code"
            all_up=false
        fi
    done
    $all_up && info "全部 6 服务运行正常" || warn "部分服务异常"
}

# ---- 增量构建 ----
quick() {
    info "增量构建 (跳过 clean + 并行编译)..."
    cd frontend && npm run build --silent 2>/dev/null && cd ..
    cd frontend-mall && npm run build --silent 2>/dev/null && cd ..
    local cpu=$(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo 4)
    ./mvnw package -DskipTests -T"$cpu"C -pl \
        yunxingcloud-usercenter,yunxingcloud-payment,yunxingcloud-order,yunxingcloud-inventory,yunxingcloud-core,yunxingcloud-gateway \
        -am -q
    info "增量构建完成"
}

# ---- 仅后端 ----
build_backend() {
    info "仅构建后端 (Maven 全模块)..."
    ./mvnw clean package -DskipTests -q
    info "后端构建完成"
}

# ---- 仅前端 ----
build_frontend() {
    info "构建 Admin 前端..."
    cd frontend && npm run build --silent 2>/dev/null && cd ..
    info "构建 Mall 前端..."
    cd frontend-mall && npm run build --silent 2>/dev/null && cd ..
    info "前端构建完成"
}

# ---- 查看日志 ----
logs() {
    ssh_exec "tail -${1:-50} $REMOTE_LOG/app.log"
}

# ---- 状态 ----
status() {
    info "检查服务状态..."
    if ssh_exec "test -f $REMOTE_APP/app.pid && kill -0 \$(cat $REMOTE_APP/app.pid) 2>/dev/null"; then
        info "应用运行中 (PID: $(ssh_exec "cat $REMOTE_APP/app.pid"))"
        ssh_exec "curl -s http://localhost:$APP_PORT/actuator/health" 2>/dev/null || true
    else
        warn "应用未运行"
    fi
}

# ---- 完整部署 ----
full() {
    info "======== 开始一键部署 yunxingcloud ========"
    build
    stop || true
    upload
    start
    health_check
    info "======== 部署完成 ========"
    echo "访问: http://$SERVER_HOST:$APP_PORT"
}

# ---- 初始化服务器环境 ----
init_server() {
    info "初始化阿里云 ECS 环境..."
    ssh_exec "
        # 安装 Java 17
        if ! command -v java &>/dev/null; then
            yum install -y java-17-openjdk java-17-openjdk-devel
        fi
        # 安装 MySQL (如果未安装)
        if ! command -v mysql &>/dev/null; then
            yum install -y mysql-server
            systemctl start mysqld
            systemctl enable mysqld
        fi
        # 创建目录
        mkdir -p $REMOTE_APP $REMOTE_LOG $DEPLOY_DIR/backup
        # 防火墙开放端口
        firewall-cmd --add-port=$APP_PORT/tcp --permanent 2>/dev/null || true
        firewall-cmd --reload 2>/dev/null || true
        echo '服务器环境初始化完成'
    "
    info "服务器环境初始化完成"
}

# ---- 数据库迁移 ----
migrate() {
    info "执行数据库迁移..."
    ssh_exec "cd $REMOTE_APP && \
        JAR=\$(ls yunxingcloud-core/target/yunxingcloud-core-*.jar 2>/dev/null | head -1) && \
        java -jar \$JAR \
            --spring.flyway.enabled=true \
            --spring.flyway.locations=classpath:db/migration \
            --spring.jpa.hibernate.ddl-auto=none \
            --spring.datasource.url='$DB_URL' \
            --spring.datasource.username='$DB_USERNAME' \
            --spring.datasource.password='$DB_PASSWORD' \
            --spring.flyway.migrate-on-start=true \
            2>&1" || warn "Flyway 迁移可能已完成"
}

# ---- 回滚 ----
rollback() {
    local BACKUP=$(ssh_exec "ls -t $DEPLOY_DIR/backup/ | head -1")
    if [ -z "$BACKUP" ]; then
        error "没有可用的备份"
    fi
    info "回滚到: $BACKUP"
    stop || true
    ssh_exec "cd $DEPLOY_DIR && tar -xzf backup/$BACKUP"
    start
    health_check
}

# ============================================
CMD=${1:-full}
case "$CMD" in
    full)          full ;;
    build)         build ;;
    quick)         quick ;;
    build-backend) build_backend ;;
    build-frontend) build_frontend ;;
    upload)        upload ;;
    start)         start ;;
    stop)          stop ;;
    restart)       restart ;;
    logs)          logs ${2:-50} ;;
    status)        status ;;
    migrate)       migrate ;;
    init)          init_server ;;
    rollback)      rollback ;;
    *)
        echo "用法: $0 {full|quick|build|build-backend|build-frontend|upload|start|stop|restart|logs|status|migrate|init|rollback}"
        echo ""
        echo "  full           - 完整部署 (构建→上传→启动→检查)"
        echo "  quick          - 增量构建部署 (跳过clean + 并行编译)"
        echo "  build          - 完整构建"
        echo "  build-backend  - 仅构建后端"
        echo "  build-frontend - 仅构建前端 (Admin + Mall)"
        echo "  upload         - 构建并上传"
        echo "  start          - 远程启动应用"
        echo "  stop           - 远程停止应用"
        echo "  restart        - 远程重启应用"
        echo "  logs           - 查看远程日志"
        echo "  status         - 查看服务状态"
        echo "  migrate        - 执行数据库迁移"
        echo "  init           - 初始化阿里云 ECS 环境"
        echo "  rollback       - 回滚到上一个备份"
        exit 1
        ;;
esac
