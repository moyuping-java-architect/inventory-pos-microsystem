@echo off
REM ============================================================
REM PSI 启动可观测性中间件 (SkyWalking)
REM ============================================================
chcp 65001 > nul
setlocal

set "PROJECT_ROOT=%~dp0"
echo [INFO] 项目根目录：%PROJECT_ROOT%

REM 检测 docker
where docker >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 未检测到 docker，请先安装 Docker Desktop
    exit /b 1
)

echo [INFO] 启动 SkyWalking + Elasticsearch...
docker compose -f "%PROJECT_ROOT%docker-compose-observability.yml" up -d

if errorlevel 1 (
    echo [ERROR] 启动失败
    exit /b 1
)

echo.
echo ============================================================
echo  启动完成，等待 ES 就绪（约 30~60s）...
echo ============================================================
echo  SkyWalking UI:        http://localhost:12900
echo  SkyWalking OAP gRPC:  localhost:11800
echo  SkyWalking OAP REST:  localhost:12800
echo  Elasticsearch:        http://localhost:9200
echo ============================================================
echo  接下来启动业务服务，请用 -javaagent 方式：
echo    set JAVA_OPTS=-javaagent:.\docker\skywalking\agent\skywalking-agent.jar -Dskywalking.config=.\docker\skywalking\agent.config
echo    java %JAVA_OPTS% -jar xxx.jar
echo ============================================================

endlocal
