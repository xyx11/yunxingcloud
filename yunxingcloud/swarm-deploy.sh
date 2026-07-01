#!/bin/bash
set -e
# ============================================
# yunxingcloud Docker Swarm 一键部署脚本
# 用法: ./swarm-deploy.sh [init|deploy|update|logs|down]
# ============================================

STACK_NAME="yunxingcloud"
COMPOSE_FILE="docker-compose-swarm.yml"
REGISTRY="${DOCKER_REGISTRY:-}"  # e.g. "ghcr.io/user/"
TAG="${TAG:-latest}"

GREEN='\033[0;32m'; BLUE='\033[0;34m'; NC='\033[0m'
info() { echo -e "${GREEN}[INFO]${NC} $1"; }

# ---- 初始化 Swarm ----
init() {
  info "初始化 Docker Swarm..."
  if docker info --format '{{.Swarm.LocalNodeState}}' 2>/dev/null | grep -q "active"; then
    info "Swarm 已运行"
  else
    docker swarm init --advertise-addr eth0 2>/dev/null || docker swarm init
    info "Swarm 已初始化"
  fi

  # 创建覆盖网络
  docker network create --driver overlay app 2>/dev/null || true
  info "网络已创建"
}

# ---- 部署/更新 ----
deploy() {
  info "构建镜像..."
  docker build --build-arg SERVICE=yunxingcloud-core --build-arg PORT=8080 \
    -t ${REGISTRY}yunxingcloud-core:${TAG} . 2>&1 | tail -3

  info "部署 Stack..."
  REGISTRY=$REGISTRY TAG=$TAG \
    docker stack deploy -c $COMPOSE_FILE --with-registry-auth $STACK_NAME

  info "等待服务就绪..."
  sleep 30
  for i in $(seq 1 30); do
    STATUS=$(docker service ls --filter "name=${STACK_NAME}_gateway" --format '{{.Replicas}}' 2>/dev/null)
    if echo "$STATUS" | grep -q "1/1"; then
      info "Gateway 就绪!"
      break
    fi
    sleep 5
  done

  docker service ls --filter "name=${STACK_NAME}_"
}

# ---- 查看日志 ----
logs() {
  local svc=${1:-gateway}
  docker service logs -f --tail 50 ${STACK_NAME}_${svc} 2>/dev/null || \
    echo "Usage: $0 logs [core|gateway|usercenter]"
}

# ---- 状态 ----
status() {
  docker stack services $STACK_NAME
  echo ""
  docker stack ps $STACK_NAME --no-trunc
}

# ---- 下线 ----
down() {
  info "移除 Stack..."
  docker stack rm $STACK_NAME
  info "已移除"
}

# ---- 滚动更新单个服务 ----
update() {
  local svc=${1:-core}
  local port=8080
  case "$svc" in
    core)       port=8080 ;;
    usercenter) port=8081 ;;
    payment)    port=8083 ;;
    order)      port=8084 ;;
    inventory)  port=8085 ;;
    gateway)    port=8090 ;;
  esac
  info "更新 $svc (端口 $port)..."
  docker build --build-arg SERVICE=yunxingcloud-${svc} --build-arg PORT=$port \
    -t ${REGISTRY}yunxingcloud-${svc}:${TAG} .
  docker service update --force --image ${REGISTRY}yunxingcloud-${svc}:${TAG} \
    ${STACK_NAME}_${svc}
  info "$svc 已更新"
}

# ============================================
CMD=${1:-deploy}
case "$CMD" in
  init)   init ;;
  deploy) init && deploy ;;
  update) update ${2:-core} ;;
  logs)   logs ${2:-gateway} ;;
  status) status ;;
  down)   down ;;
  *)
    echo "用法: $0 {init|deploy|update [svc]|logs [svc]|status|down}"
    echo ""
    echo "  deploy  - 初始化 Swarm + 构建 + 部署全栈"
    echo "  update  - 滚动更新单个服务（不中断）"
    echo "  logs    - 查看服务日志"
    echo "  status  - 查看 Stack 状态"
    echo "  down    - 移除 Stack"
    ;;
esac
