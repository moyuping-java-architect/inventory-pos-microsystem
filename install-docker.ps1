#Requires -RunAsAdministrator
<#
  Docker Desktop + WSL2 一键安装脚本
  用法：右键 PowerShell → 以管理员身份运行，然后执行：
  .\install-docker.ps1
#>

param(
    [string]$DockerInstallerPath = "$env:TEMP\DockerDesktopInstaller.exe"
)

function Write-Step($msg) {
    Write-Host "`n[STEP] $msg" -ForegroundColor Cyan
}

function Test-Command($cmd) {
    return [bool](Get-Command $cmd -ErrorAction SilentlyContinue)
}

# 1. 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
    [Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Error "请以管理员身份运行 PowerShell 后再执行此脚本。"
    exit 1
}

# 2. 启用 WSL 和虚拟机平台
Write-Step "启用 WSL 和虚拟机平台功能（Windows 11 需要）"
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart | Out-Null
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart | Out-Null
Write-Host "功能已启用，可能需要重启后才能完全生效。" -ForegroundColor Yellow

# 3. 设置 WSL 默认版本为 2
Write-Step "设置 WSL 默认版本为 2"
try {
    wsl --set-default-version 2 | Out-Null
    wsl --update --web-download | Out-Null
} catch {
    Write-Host "WSL 更新跳过，将在 Docker 安装过程中自动处理。" -ForegroundColor Yellow
}

# 4. 检查 Docker 安装包是否存在，不存在则下载
if (-not (Test-Path $DockerInstallerPath)) {
    Write-Step "下载 Docker Desktop 安装包"
    $url = "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe"
    curl.exe -L -o $DockerInstallerPath $url
    if (-not (Test-Path $DockerInstallerPath)) {
        Write-Error "下载 Docker Desktop 安装包失败，请检查网络。"
        exit 1
    }
}

# 5. 静默安装 Docker Desktop
Write-Step "开始静默安装 Docker Desktop（可能需要几分钟）"
$proc = Start-Process -FilePath $DockerInstallerPath -ArgumentList "install --quiet" -Wait -PassThru
if ($proc.ExitCode -ne 0) {
    Write-Error "Docker Desktop 安装失败，退出码: $($proc.ExitCode)"
    exit 1
}
Write-Host "Docker Desktop 安装完成。" -ForegroundColor Green

# 6. 配置国内镜像加速器
Write-Step "配置 Docker 镜像加速器"
$dockerConfigDir = "$env:USERPROFILE\.docker"
if (-not (Test-Path $dockerConfigDir)) {
    New-Item -ItemType Directory -Path $dockerConfigDir | Out-Null
}
$daemonJson = "$dockerConfigDir\daemon.json"
$config = @{
    registry-mirrors = @(
        "https://docker.mirrors.ustc.edu.cn",
        "https://hub-mirror.c.163.com",
        "https://mirror.ccs.tencentyun.com"
    )
} | ConvertTo-Json -Depth 3
Set-Content -Path $daemonJson -Value $config -Encoding UTF8
Write-Host "镜像加速器已写入: $daemonJson" -ForegroundColor Green

# 7. 启动 Docker Desktop
Write-Step "启动 Docker Desktop"
$dockerPath = "$env:ProgramFiles\Docker\Docker\Docker Desktop.exe"
if (Test-Path $dockerPath) {
    Start-Process $dockerPath
    Write-Host "Docker Desktop 已启动，首次启动可能需要初始化 WSL2，请稍等。" -ForegroundColor Green
} else {
    Write-Host "未找到 Docker Desktop.exe，请手动从开始菜单启动。" -ForegroundColor Yellow
}

Write-Host "`n安装脚本执行完毕。建议重启电脑后执行: docker --version" -ForegroundColor Green
