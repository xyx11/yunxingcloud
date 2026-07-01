#!/bin/bash
set -e
# ============================================
# ECS 服务器初始化脚本
# 复制到 ECS 执行: bash setup-ecs.sh
# ============================================

GREEN='\033[0;32m'; NC='\033[0m'
info() { echo -e "${GREEN}[INFO]${NC} $1"; }

info "1. 停止 Apache (httpd)..."
systemctl stop httpd 2>/dev/null || true
systemctl disable httpd 2>/dev/null || true

info "2. 安装 Nginx..."
if ! command -v nginx &>/dev/null; then
    yum install -y nginx
fi

info "3. 配置 Nginx 反向代理..."
cat > /etc/nginx/conf.d/yunxingcloud.conf << 'NGINX'
upstream yunxingcloud_backend {
    server 127.0.0.1:8090 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

server {
    listen 80;
    server_name www.yunxingcloud.com;

    client_max_body_size 10m;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1024;
    gzip_vary on;

    location /assets/ {
        proxy_pass http://yunxingcloud_backend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location /api/ {
        proxy_pass http://yunxingcloud_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/ {
        proxy_pass http://yunxingcloud_backend;
        proxy_set_header Host $host;
    }

    location /oauth2/ {
        proxy_pass http://yunxingcloud_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        proxy_pass http://yunxingcloud_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX

# 移除默认配置
rm -f /etc/nginx/conf.d/default.conf

info "4. 启动 Nginx..."
nginx -t && systemctl restart nginx && systemctl enable nginx

info "5. 创建应用目录..."
mkdir -p /opt/yunxingcloud/app /opt/yunxingcloud/logs

info "6. 检查防火墙..."
firewall-cmd --add-service=http --permanent 2>/dev/null || true
firewall-cmd --add-service=https --permanent 2>/dev/null || true
firewall-cmd --reload 2>/dev/null || true

echo ""
echo "============================================"
echo "  ECS 初始化完成!"
echo "  下一步: 上传全部 6 服务 JAR 并启动"
echo ""
echo "  # 上传 JAR 到 ECS"
echo "  scp yunxingcloud-*/target/yunxingcloud-*-[0-9]*.jar root@HOST:/opt/yunxingcloud/app/"
echo "  # 或使用 deploy.yml GitHub Actions 自动部署"
echo ""
echo "  # 启动顺序 (Gateway 最后):"
echo "  ssh root@HOST"
echo "  cd /opt/yunxingcloud/app"
echo "  for svc in usercenter payment order inventory core gateway; do"
echo "    nohup java -jar yunxingcloud-\$svc-*.jar --spring.profiles.active=prod > ../logs/\$svc.log 2>&1 &"
echo "    sleep 5"
echo "  done"
echo "============================================"
