@echo off
REM ============================================================
REM PSI 启动单个服务并挂载 SkyWalking Agent
REM 使用方法:  start-with-trace.bat psi-goods 8082
REM ============================================================
chcp 65001 > nul
setlocal

set "SERVICE_NAME=%~1"
set "SERVICE_PORT=%~2"

if "%SERVICE_NAME%"=="" (
    echo [ERROR] 用法: start-with-trace.bat ^<service-name^> [port]
    echo   示例: start-with-trace.bat psi-goods 8082
    exit /b 1
)

if "%SERVICE_PORT%"=="" set "SERVICE_PORT=8080"

set "PROJECT_ROOT=%~dp0"
set "JAR_FILE=%PROJECT_ROOT%%SERVICE_NAME%\target\%SERVICE_NAME%-1.0.0.jar"
set "AGENT_JAR=%PROJECT_ROOT%docker\skywalking\agent\skywalking-agent.jar"
set "AGENT_CONFIG=%PROJECT_ROOT%docker\skywalking\agent.config"

if not exist "%JAR_FILE%" (
    echo [ERROR] JAR 不存在：%JAR_FILE%
    exit /b 1
)
if not exist "%AGENT_JAR%" (
    echo [WARN] SkyWalking Agent 不存在：%AGENT_JAR%
    echo        请先下载：https://archive.apache.org/dist/skywalking/10.1.0/apache-skywalking-java-agent-10.1.0.tgz
    echo        解压到 docker\skywalking\agent\ 目录
    set "JAVA_OPTS="
) else (
    set "JAVA_OPTS=-javaagent:%AGENT_JAR% -Dskywalking.config=%AGENT_CONFIG%"
)

set "SW_AGENT_SERVICE_NAME=%SERVICE_NAME%"
set "SW_AGENT_COLLECTOR_BACKEND_SERVICES=localhost:11800"

echo [INFO] 启动服务：%SERVICE_NAME%
echo [INFO] 监听端口：%SERVICE_PORT%
echo [INFO] 上报到 SkyWalking：localhost:11800
echo.

java %JAVA_OPTS% -jar "%JAR_FILE%" --server.port=%SERVICE_PORT%

endlocal
