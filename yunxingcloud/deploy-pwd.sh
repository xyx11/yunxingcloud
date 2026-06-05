#!/bin/bash
set -e
# ============================================
# 密码认证部署脚本
# ============================================
HOST=47.95.111.21
PORT=22
USER=root
PASS=Hpi123456
DEPLOY_DIR=/opt/yunxingcloud
SSH="sshpass -p $PASS ssh -o StrictHostKeyChecking=no -p $PORT $USER@$HOST"
SCP="sshpass -p $PASS scp -o StrictHostKeyChecking=no -P $PORT"

GREEN='\033[0;32m'; NC='\033[0m'
info() { echo -e "${GREEN}[INFO]${NC} $1"; }

info "1. 上传 setup-ecs.sh 到服务器..."
$SCP setup-ecs.sh $USER@$HOST:/tmp/

info "2. 执行初始化脚本 (停止Apache + 安装Nginx + 配置)..."
$SSH "bash /tmp/setup-ecs.sh"

info "3. 创建目录..."
$SSH "mkdir -p $DEPLOY_DIR/app $DEPLOY_DIR/logs $DEPLOY_DIR/backup"

info "4. 上传 JAR..."
$SCP yunxingcloud-core/target/yunxingcloud-core-0.0.1-SNAPSHOT.jar $USER@$HOST:$DEPLOY_DIR/app/

info "5. 停止旧进程 + 启动新应用..."
$SSH "pkill -f yunxingcloud-core || true; sleep 2"
$SSH "cd $DEPLOY_DIR/app && nohup java -jar yunxingcloud-core-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > $DEPLOY_DIR/logs/app.log 2>&1 &"

info "6. 等待应用启动..."
sleep 8
for i in $(seq 1 15); do
  code=$($SSH "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health" 2>/dev/null || echo "000")
  if [ "$code" = "200" ]; then
    info "应用启动成功! (HTTP 200)"
    break
  fi
  echo -n "."
  sleep 2
done

info "7. 验证外部访问..."
echo ""
echo -n "  http://www.yunxingcloud.com → "
curl -s -o /dev/null -w "HTTP %{http_code}" http://www.yunxingcloud.com/ 2>/dev/null || echo "请等待DNS生效"
echo ""
echo -n "  http://$HOST:80 → "
curl -s -o /dev/null -w "HTTP %{http_code}" http://$HOST/ 2>/dev/null
echo ""

info "部署完成!"
echo "  访问: http://www.yunxingcloud.com"
echo "  文档: http://www.yunxingcloud.com/doc.html"
echo "  日志: ssh root@$HOST 'tail -f $DEPLOY_DIR/logs/app.log'"
