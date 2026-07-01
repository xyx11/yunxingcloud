#!/bin/bash
set -e
# ============================================
# 生产环境初始化: MySQL + systemd + prod模式
# ============================================
DB_PASS="${DB_PASSWORD:?Set DB_PASSWORD}"
JWT_SECRET="${JWT_SECRET:-yunxingcloud-prod-jwt-secret-change-me}"
APP_DIR="/opt/yunxingcloud"
JAVA_BIN="${JAVA_HOME:-/usr/bin}/java"
GREEN='\033[0;32m'; NC='\033[0m'
info() { echo -e "${GREEN}[INFO]${NC} $1"; }

info "1. 安装 MySQL 8..."
if ! command -v mysql &>/dev/null; then
    yum install -y mysql-server
fi
systemctl start mysqld 2>/dev/null || systemctl start mysql 2>/dev/null || true
systemctl enable mysqld 2>/dev/null || systemctl enable mysql 2>/dev/null || true

# 首次启动获取临时密码
TMP_PWD=$(grep 'temporary password' /var/log/mysqld.log 2>/dev/null | tail -1 | awk '{print $NF}' || echo "")
if [ -n "$TMP_PWD" ]; then
    mysqladmin -u root -p"$TMP_PWD" password "$DB_PASS" 2>/dev/null || true
fi

info "2. 创建数据库..."
mysql -u root -p"$DB_PASS" << SQL 2>/dev/null || mysql -u root << SQL 2>/dev/null
CREATE DATABASE IF NOT EXISTS yunxingcloud_core CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS yunxingcloud_usercenter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS yunxingcloud_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS yunxingcloud_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS yunxingcloud_inventory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'yunxingcloud'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON yunxingcloud_core.* TO 'yunxingcloud'@'localhost';
GRANT ALL PRIVILEGES ON yunxingcloud_usercenter.* TO 'yunxingcloud'@'localhost';
GRANT ALL PRIVILEGES ON yunxingcloud_payment.* TO 'yunxingcloud'@'localhost';
GRANT ALL PRIVILEGES ON yunxingcloud_order.* TO 'yunxingcloud'@'localhost';
GRANT ALL PRIVILEGES ON yunxingcloud_inventory.* TO 'yunxingcloud'@'localhost';
FLUSH PRIVILEGES;
SQL

info "3. 创建 systemd 服务..."
SERVICES=("usercenter:8081" "payment:8083" "order:8084" "inventory:8085" "core:8080" "gateway:8090")

for svc_port in "${SERVICES[@]}"; do
    svc="${svc_port%%:*}"
    port="${svc_port##*:}"
    db_name="yunxingcloud_${svc}"
    [ "$svc" = "core" ] && db_name="yunxingcloud_core"

    cat > "/etc/systemd/system/yunxingcloud-${svc}.service" << SERVICE
[Unit]
Description=yunxingcloud ${svc} Service
After=network.target mysqld.service mysql.service
Wants=mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=$APP_DIR/app
ExecStart=$JAVA_BIN -Xms512m -Xmx1024m -XX:+UseG1GC -Duser.timezone=Asia/Shanghai \\
    -jar $APP_DIR/app/yunxingcloud-${svc}.jar \\
    --spring.profiles.active=prod \\
    --server.port=${port} \\
    --spring.datasource.url=jdbc:mysql://localhost:3306/${db_name}?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai \\
    --spring.datasource.username=yunxingcloud \\
    --spring.datasource.password=$DB_PASS \\
    --jwt.secret=$JWT_SECRET
ExecStop=/bin/kill -15 \$MAINPID
Restart=always
RestartSec=10
StandardOutput=append:$APP_DIR/logs/${svc}-stdout.log
StandardError=append:$APP_DIR/logs/${svc}-stderr.log

[Install]
WantedBy=multi-user.target
SERVICE
done

systemctl daemon-reload
for svc_port in "${SERVICES[@]}"; do
    svc="${svc_port%%:*}"
    systemctl enable "yunxingcloud-${svc}" 2>/dev/null || true
done

info "4. 启动全部服务..."
for svc_port in "${SERVICES[@]}"; do
    svc="${svc_port%%:*}"
    port="${svc_port##*:}"
    pkill -f "yunxingcloud-${svc}" 2>/dev/null || true
    systemctl restart "yunxingcloud-${svc}" 2>/dev/null || true
    sleep 3
done

info "5. 等待启动..."
for svc_port in "${SERVICES[@]}"; do
    svc="${svc_port%%:*}"
    port="${svc_port##*:}"
    for i in $(seq 1 15); do
        code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/actuator/health" 2>/dev/null || echo "000")
        if [ "$code" = "200" ]; then
            info "  ${svc}:${port} UP"
            break
        fi
        sleep 2
    done
done

info "6. 更新 Nginx (反向代理 → Gateway:8090)..."
cp "$SCRIPT_DIR/nginx-production.conf" /etc/nginx/conf.d/yunxingcloud.conf 2>/dev/null || {
    cat > /etc/nginx/conf.d/yunxingcloud.conf << 'NGINX'
upstream backend { server 127.0.0.1:8090; keepalive 32; }
server {
    listen 80;
    server_name _;
    client_max_body_size 10m;
    gzip on;
    gzip_types text/css application/json application/javascript text/xml;
    gzip_min_length 1024;
    gzip_vary on;
    location /assets/ { proxy_pass http://backend; expires 30d; }
    location /api/ { proxy_pass http://backend; proxy_http_version 1.1; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; proxy_set_header X-Forwarded-Proto $scheme; }
    location / { proxy_pass http://backend; proxy_http_version 1.1; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; proxy_set_header X-Forwarded-Proto $scheme; }
}
NGINX
}
nginx -t && systemctl restart nginx

echo ""
info "============================================"
info "  生产环境部署完成!"
info "  数据库: 5 个 (yunxingcloud_core/usercenter/payment/order/inventory)"
info "  服务: systemctl {start|stop|restart} yunxingcloud-{svc}"
info "  端口: 8080(core) 8081(usercenter) 8083(payment) 8084(order) 8085(inventory) 8090(gateway)"
info "============================================"