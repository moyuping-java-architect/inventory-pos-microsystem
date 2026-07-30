#!/bin/sh
# ============================================================
# PSI 通用容器启动脚本（支持 SkyWalking Agent 注入）
# ============================================================
# 通过环境变量 JAVA_OPTS 接收启动参数
# 通过环境变量 SW_AGENT_NAME 配置服务名
# ============================================================
set -e

# 1. 如果开启了 SkyWalking 但 agent 不存在，警告但不退出
if echo "$JAVA_OPTS" | grep -q "skywalking-agent.jar"; then
    AGENT_PATH=$(echo "$JAVA_OPTS" | grep -oE '\-javaagent:[^ ]+' | head -1 | cut -d: -f2)
    if [ ! -f "$AGENT_PATH" ]; then
        echo "[WARN] SkyWalking agent not found at $AGENT_PATH"
        echo "[WARN] 请先把 agent 挂载到 /opt/skywalking/agent/"
        # 移除 -javaagent 参数
        JAVA_OPTS=$(echo "$JAVA_OPTS" | sed -E 's/-javaagent:[^ ]+//g')
    else
        echo "[INFO] SkyWalking agent attached: $AGENT_PATH"
        echo "[INFO] Service name: ${SW_AGENT_NAME:-unknown}"
        echo "[INFO] Collector:    ${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-skywalking-oap:11800}"
    fi
fi

# 2. GC 日志（面试加分项）
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M"

# 3. OOM 时自动 dump
OOM_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/oom.hprof"

# 4. 启动
echo "[INFO] Starting JVM with JAVA_OPTS=$JAVA_OPTS"
exec java $GC_OPTS $OOM_OPTS $JAVA_OPTS -jar /app/app.jar "$@"
