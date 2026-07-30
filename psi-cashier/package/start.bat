@echo off
chcp 65001 >nul
title PSI Cashier

REM ===== 数据库安全：创建隐藏目录 =====
REM 数据库存放到 C:\ProgramData\PSI\sysconfig\（系统目录，普通用户不可见）
set "PSI_DB_DIR=%ProgramData%\PSI\sysconfig"
if not exist "%PSI_DB_DIR%" (
    mkdir "%PSI_DB_DIR%"
    REM 设置目录为隐藏
    attrib +H "%PSI_DB_DIR%"
)

REM 设置数据库路径环境变量（应用会读取此变量）
set "PSI_DB_PATH=%PSI_DB_DIR%\syscfg.dat"

REM 设置隐藏属性（如果数据库文件已存在）
if exist "%PSI_DB_PATH%" (
    attrib +H "%PSI_DB_PATH%"
)

REM 检查其他目录
if not exist "data" mkdir data
if not exist "backup" mkdir backup
if not exist "logs" mkdir logs

REM 设置 Java 路径（使用打包的绿色 JRE）
set JAVA_HOME=%~dp0jre
set PATH=%JAVA_HOME%\bin;%PATH%

REM 启动应用
echo Starting PSI Cashier...
echo.
echo Database secured at system protected location.
echo.

java -Xms256m -Xmx512m ^
  -jar psi-cashier.jar ^
  --spring.config.location=application.properties ^
  --psi.db.auto-init=true ^
  --psi.db.secure=true ^
  --psi.db.path=%PSI_DB_PATH%

if %errorlevel% neq 0 (
    echo.
    echo PSI Cashier stopped with error code: %errorlevel%
)

pause
