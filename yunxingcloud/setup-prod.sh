#!/bin/bash
set -e
# ============================================
# 生产环境初始化: MySQL + systemd + prod模式
# ============================================
DB_PASS="Yunxingcloud@2026"
JWT_SECRET="yunxingcloud-prod-jwt-secret-2026-very-long-key-change-me"
APP_DIR="/opt/yunxingcloud"
JAVA_BIN="/usr/lib/jvm/java-17-openjdk-17.0.19.0.10-1.0.2.1.al8.x86_64/bin/java"
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
CREATE DATABASE IF NOT EXISTS sso_yunxingcloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'yunxingcloud'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON sso_yunxingcloud.* TO 'yunxingcloud'@'localhost';
FLUSH PRIVILEGES;
SQL

info "3. 创建 systemd 服务..."
cat > /etc/systemd/system/yunxingcloud.service << SERVICE
[Unit]
Description=yunxingcloud Application
After=network.target mysqld.service mysql.service
Wants=mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=$APP_DIR/app
ExecStart=$JAVA_BIN -Xms512m -Xmx1024m -XX:+UseG1GC -Duser.timezone=Asia/Shanghai \\
    -jar $APP_DIR/app/yunxingcloud-core-0.0.1-SNAPSHOT.jar \\
    --spring.profiles.active=prod \\
    --server.port=8080 \\
    --spring.datasource.url=jdbc:mysql://localhost:3306/sso_yunxingcloud?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai \\
    --spring.datasource.username=yunxingcloud \\
    --spring.datasource.password=$DB_PASS \\
    --jwt.secret=$JWT_SECRET
ExecStop=/bin/kill -15 \$MAINPID
Restart=always
RestartSec=10
StandardOutput=append:$APP_DIR/logs/stdout.log
StandardError=append:$APP_DIR/logs/stderr.log

[Install]
WantedBy=multi-user.target
SERVICE

systemctl daemon-reload
systemctl enable yunxingcloud

info "4. 停止旧进程，启动 systemd 服务..."
pkill -f yunxingcloud-core 2>/dev/null || true
sleep 2
systemctl restart yunxingcloud

info "5. 等待启动..."
for i in $(seq 1 20); do
    code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
    if [ "$code" = "200" ]; then
        info "应用启动成功!"
        break
    fi
    echo -n "."
    sleep 2
done

info "6. 更新 Nginx 生产配置..."
cat > /etc/nginx/conf.d/yunxingcloud.conf << 'NGINX'
upstream backend { server 127.0.0.1:8080; keepalive 32; }

server {
    listen 80;
    server_name www.yunxingcloud.com 47.95.111.21;
    client_max_body_size 10m;

    gzip on;
    gzip_types text/css application/json application/javascript text/xml;
    gzip_min_length 1024;
    gzip_vary on;

    location /assets/ {
        proxy_pass http://backend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location /api/ {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX
nginx -t && systemctl restart nginx

echo ""
info "============================================"
info "  生产环境部署完成!"
info "  数据库: MySQL 8 (sso_yunxingcloud)"
info "  服务: systemctl {start|stop|restart} yunxingcloud"
info "  日志: journalctl -u yunxingcloud -f"
info "  访问: http://47.95.111.21"
info "============================================"
