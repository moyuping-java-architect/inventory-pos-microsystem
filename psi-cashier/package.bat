@echo off
chcp 65001 >nul
echo ================================================
echo PSI Cashier 打包脚本
echo ================================================

setlocal enabledelayedexpansion

REM 1. 编译项目
echo [1/5] 编译项目...
cd /d "%~dp0"
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)
echo 编译成功！

REM 2. 创建打包目录
echo [2/5] 创建打包目录...
set "PACKAGE_DIR=%~dp0package"
if exist "%PACKAGE_DIR%" rmdir /s /q "%PACKAGE_DIR%"
mkdir "%PACKAGE_DIR%"
mkdir "%PACKAGE_DIR%\jre"
mkdir "%PACKAGE_DIR%\data"
mkdir "%PACKAGE_DIR%\backup"
mkdir "%PACKAGE_DIR%\logs"

REM 3. 复制 JAR
echo [3/5] 复制 JAR 文件...
copy "%~dp0target\psi-cashier-1.0.0.jar" "%PACKAGE_DIR%\psi-cashier.jar" /Y

REM 4. 复制配置文件
echo [4/5] 复制配置文件...
copy "%~dp0src\main\resources\application-docker.properties" "%PACKAGE_DIR%\application.properties" /Y

REM 修改配置为单机模式
(
    echo psi.mode=standalone
    echo psi.sync.enabled=false
    echo psi.mq.enabled=false
    echo psi.flow.enabled=false
    echo psi.print.type=usb
    echo server.port=8088
    echo spring.datasource.url=jdbc:sqlite:data/myp.db
    echo spring.datasource.driver-class-name=org.sqlite.JDBC
    echo spring.datasource.username=admin
    echo spring.datasource.password=123456
    echo mybatis-plus.configuration.map-underscore-to-camel-case=true
    echo mybatis-plus.global-config.db-config.id-type=auto
    echo logging.file.path=logs/
    echo logging.level.com.psi=INFO
) > "%PACKAGE_DIR%\application.properties"

REM 5. 复制绿色 JRE（需要先下载）
echo [5/5] 复制绿色 JRE...
set "JRE_SOURCE=%~dp0jre"
if exist "%JRE_SOURCE%" (
    xcopy "%JRE_SOURCE%" "%PACKAGE_DIR%\jre" /E /I /Y
    echo JRE 复制成功！
) else (
    echo WARNING: JRE 目录不存在，请先下载绿色 JRE 到 %JRE_SOURCE%
    echo 下载地址: https://adoptium.net/temurin/releases/?version=21
    echo 选择: JRE, Windows, x64
)

echo.
echo 打包完成！
echo 目录: %PACKAGE_DIR%
echo.
echo 下一步：
echo 1. 下载绿色 JRE 到 %JRE_SOURCE%
echo 2. 安装 Inno Setup
echo 3. 运行 PSI_Setup.iss 生成安装包
echo.
pause
