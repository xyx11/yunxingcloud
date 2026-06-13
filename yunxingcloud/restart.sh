#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
echo "========== 重启 yunxingcloud =========="

# 停止旧进程
echo "1. 停止旧服务..."
lsof -ti :8080 | xargs kill -9 2>/dev/null || true
lsof -ti :5173 | xargs kill -9 2>/dev/null || true
sleep 2

# 构建前端
echo "2. 构建前端..."
cd "$ROOT/frontend" && npx vite build 2>&1 | tail -1

# 构建后端
echo "3. 构建后端..."
cd "$ROOT" && ./mvnw install -pl yunxingcloud-core -am -DskipTests -q

# 启动后端
echo "4. 启动后端 (8080)..."
nohup java -jar "$ROOT/yunxingcloud-core/target/yunxingcloud-core-0.0.1-SNAPSHOT.jar" > /tmp/yunxingcloud.log 2>&1 &
sleep 8

# 启动前端
echo "5. 启动前端 (5173)..."
cd "$ROOT/frontend" && nohup npx vite --port 5173 --host > /tmp/vite.log 2>&1 &
sleep 3

# 验证
echo ""
echo "========== 验证 =========="
curl -s -o /dev/null -w "后端: HTTP %{http_code}\n" http://localhost:8080/
curl -s -o /dev/null -w "前端: HTTP %{http_code}\n" http://localhost:5173/ 2>/dev/null || echo "前端: 启动中..."
echo ""
echo "http://localhost:5173"
echo "http://localhost:8080"
echo "admin / admin123"
