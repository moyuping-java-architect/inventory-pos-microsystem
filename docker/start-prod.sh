#!/bin/bash
# =====================================================================
# PSI 生产环境一键启动脚本
# =====================================================================
# 功能：
#   1. 检查 Docker 和 Docker Compose 是否安装
#   2. 启动基础设施（MySQL, Redis, RabbitMQ, Nacos）
#   3. 启动可观测性栈（Elasticsearch, SkyWalking OAP, SkyWalking UI）
#   4. 启动核心业务服务
#   5. 等待所有服务就绪
#   6. 输出服务访问地址
#
# 使用方法：
#   chmod +x start-prod.sh
#   ./start-prod.sh
# =====================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( dirname "$SCRIPT_DIR" )"
COMPOSE_FILE="$PROJECT_DIR/docker-compose-prod.yml"

# 服务列表（按启动顺序）
INFRA_SERVICES=("mysql" "redis" "rabbitmq" "nacos")
OBS_SERVICES=("elasticsearch" "skywalking-oap" "skywalking-ui")
BIZ_SERVICES=("psi-gateway" "psi-system" "psi-goods" "psi-sale" "psi-purchase" "psi-stock")

# 打印带颜色的日志
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# 分隔线
print_separator() {
    echo ""
    echo "================================================================"
    echo "$1"
    echo "================================================================"
    echo ""
}

# 检查 Docker 是否安装
check_docker() {
    log_info "检查 Docker 环境..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        log_info "安装参考：https://docs.docker.com/engine/install/"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        log_error "Docker 守护进程未运行，请先启动 Docker"
        exit 1
    fi
    
    log_success "Docker 环境正常"
}

# 检查 Docker Compose 是否安装
check_docker_compose() {
    log_info "检查 Docker Compose..."
    
    if docker compose version &> /dev/null; then
        log_success "Docker Compose 插件已安装"
        COMPOSE_CMD="docker compose"
    elif command -v docker-compose &> /dev/null; then
        log_success "docker-compose 已安装"
        COMPOSE_CMD="docker-compose"
    else
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
}

# 等待服务健康检查通过
wait_for_healthy() {
    local service_name=$1
    local max_attempts=${2:-60}
    local wait_seconds=${3:-5}
    
    log_info "等待 $service_name 启动..."
    
    for i in $(seq 1 $max_attempts); do
        local status
        status=$(docker inspect --format='{{.State.Health.Status}}' "$service_name" 2>/dev/null || echo "starting")
        
        if [ "$status" = "healthy" ]; then
            log_success "$service_name 启动完成"
            return 0
        fi
        
        if [ "$status" = "unhealthy" ]; then
            log_warn "$service_name 状态: unhealthy，继续等待..."
        fi
        
        sleep $wait_seconds
    done
    
    log_warn "$service_name 等待超时（${max_attempts}次），继续下一步（可能启动较慢）"
    return 1
}

# 检查 compose 文件是否存在
check_compose_file() {
    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "Compose 文件不存在: $COMPOSE_FILE"
        exit 1
    fi
    log_info "使用 Compose 文件: $COMPOSE_FILE"
}

# 主函数
main() {
    print_separator "PSI 生产环境启动脚本"
    
    # 1. 环境检查
    log_info "第 1 步：环境检查"
    check_docker
    check_docker_compose
    check_compose_file
    echo ""
    
    # 2. 启动基础设施
    print_separator "第 2 步：启动基础设施（MySQL, Redis, RabbitMQ, Nacos）"
    log_info "启动基础设施服务..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" up -d mysql redis rabbitmq nacos
    
    # 等待 MySQL 就绪
    wait_for_healthy "psi-mysql" 30 5
    
    # 执行 MySQL 初始化
    log_info "执行 MySQL 数据库初始化..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" up mysql-init || true
    
    # 等待 Redis 就绪
    wait_for_healthy "psi-redis" 20 5
    
    # 等待 RabbitMQ 就绪
    wait_for_healthy "psi-rabbitmq" 20 5
    
    # 等待 Nacos 就绪
    wait_for_healthy "psi-nacos" 30 5
    
    log_success "基础设施启动完成"
    
    # 3. 启动可观测性栈
    print_separator "第 3 步：启动可观测性栈（Elasticsearch, SkyWalking）"
    log_info "启动 Elasticsearch..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" up -d elasticsearch
    wait_for_healthy "psi-elasticsearch" 40 5
    
    log_info "启动 SkyWalking OAP..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" up -d skywalking-oap
    wait_for_healthy "psi-skywalking-oap" 40 5
    
    log_info "启动 SkyWalking UI..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" up -d skywalking-ui
    wait_for_healthy "psi-skywalking-ui" 20 5
    
    log_success "可观测性栈启动完成"
    
    # 4. 启动核心业务服务
    print_separator "第 4 步：启动核心业务服务"
    log_info "启动所有核心业务服务..."
    $COMPOSE_CMD -f "$COMPOSE_FILE" up -d psi-gateway psi-system psi-goods psi-sale psi-purchase psi-stock
    
    log_info "业务服务正在启动，这可能需要 3-5 分钟..."
    log_info "你可以通过以下命令查看日志："
    log_info "  docker logs -f psi-gateway"
    log_info "  docker logs -f psi-system"
    log_info "  docker logs -f psi-goods"
    echo ""
    
    # 5. 等待业务服务就绪（可选，超时就跳过）
    log_info "等待业务服务就绪（最多等待 5 分钟）..."
    for svc in "${BIZ_SERVICES[@]}"; do
        wait_for_healthy "$svc" 30 10 || log_warn "$svc 仍在启动中，不影响使用"
    done
    
    # 6. 输出访问地址
    print_separator "启动完成！"
    
    echo -e "${GREEN}服务访问地址：${NC}"
    echo ""
    echo "  📋 管理后台:      http://$(hostname -I | awk '{print $1}'):8097"
    echo "  🌐 API 网关:      http://$(hostname -I | awk '{print $1}'):8081"
    echo "  🔍 SkyWalking:   http://$(hostname -I | awk '{print $1}'):12900"
    echo "  📦 Nacos:        http://$(hostname -I | awk '{print $1}'):8848/nacos"
    echo "  🐰 RabbitMQ:     http://$(hostname -I | awk '{print $1}'):15672 (admin/admin)"
    echo ""
    
    echo -e "${GREEN}常用命令：${NC}"
    echo "  查看所有服务状态:  docker compose -f docker-compose-prod.yml ps"
    echo "  查看服务日志:      docker logs -f <服务名>"
    echo "  停止所有服务:      docker compose -f docker-compose-prod.yml down"
    echo "  重启单个服务:      docker compose -f docker-compose-prod.yml restart <服务名>"
    echo ""
    
    echo -e "${YELLOW}提示：${NC}"
    echo "  - 首次启动较慢，请耐心等待 3-5 分钟"
    echo "  - 如果服务启动失败，查看日志排查：docker logs -f <服务名>"
    echo "  - SkyWalking 需要有请求流量才会显示数据"
    echo ""
}

main
