#!/usr/bin/env pwsh
# 一键构建所有 Java 微服务 + Go 库存服务，并打包 Docker 镜像

$ErrorActionPreference = "Stop"
$root = "e:\spring boot\psi-parent"
Set-Location $root

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 1: Maven 编译安装所有 Java 模块" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
mvn clean install -DskipTests

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 2: 构建 Go 库存服务镜像" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Set-Location "$root\psi-stock-go"
docker build -t psi-stock-go:latest .

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 3: 构建所有 Java 微服务镜像" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Set-Location $root
docker compose -f docker-compose-full.yml build

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "构建完成，可以执行启动命令：" -ForegroundColor Green
Write-Host "docker compose -f docker-compose-full.yml up -d" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
