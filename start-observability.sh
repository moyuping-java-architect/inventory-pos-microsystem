#!/bin/bash
# ============================================================
# PSI 启动可观测性中间件 (SkyWalking)
# ============================================================
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
echo "[INFO] 项目根目录：${PROJECT_ROOT}"

# 检测 docker
if ! command -v docker &> /dev/null; then
  echo "[ERROR] 未检测到 docker，请先安装 Docker"
  exit 1
fi

echo "[INFO] 启动 SkyWalking + Elasticsearch..."
docker compose -f "${PROJECT_ROOT}/docker-compose-observability.yml" up -d

cat <<'EOF'

============================================================
 启动完成，等待 ES 就绪（约 30~60s）...
============================================================
 SkyWalking UI:        http://localhost:12900
 SkyWalking OAP gRPC:  localhost:11800
 SkyWalking OAP REST:  localhost:12800
 Elasticsearch:        http://localhost:9200
============================================================
 接下来启动业务服务，请用 -javaagent 方式：

 export SW_AGENT_SERVICE_NAME=psi-goods
 export SW_AGENT_COLLECTOR_BACKEND_SERVICES=localhost:11800
 java -javaagent:/path/to/skywalking-agent.jar \
      -Dskywalking.config=/path/to/agent.config \
      -jar xxx.jar
============================================================
EOF
