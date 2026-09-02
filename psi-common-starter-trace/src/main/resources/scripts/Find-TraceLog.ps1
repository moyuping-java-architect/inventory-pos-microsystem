<#
.SYNOPSIS
TraceId 日志查询工具

.DESCRIPTION
根据日期和 traceId 查询日志，支持日志类型筛选

.PARAMETER TraceId
要查询的 traceId

.PARAMETER Date
日期：yyyyMMdd | yyyy-MM-dd | today | yesterday | -1d

.PARAMETER LogType
日志类型：all(全部) | error(错误日志) | normal(正常日志)

.EXAMPLE
.\Find-TraceLog.ps1 "abc123"
查询今天的所有日志

.EXAMPLE
.\Find-TraceLog.ps1 "abc123" "20251230"
查询指定日期的所有日志

.EXAMPLE
.\Find-TraceLog.ps1 "abc123" "yesterday" "error"
查询昨天的错误日志

.EXAMPLE
.\Find-TraceLog.ps1 -TraceId "abc123" -Date "today" -LogType "normal"
查询今天的正常日志
#>

param(
    [Parameter(Mandatory=$true, Position=0)]
    [string]$TraceId,
    
    [Parameter(Position=1)]
    [string]$Date = (Get-Date -Format "yyyyMMdd"),
    
    [Parameter(Position=2)]
    [ValidateSet("all", "error", "normal")]
    [string]$LogType = "all"
)

function Convert-Date {
    param([string]$InputDate)
    
    switch ($InputDate.ToLower()) {
        "today" { return Get-Date -Format "yyyyMMdd" }
        "yesterday" { return (Get-Date).AddDays(-1) -Format "yyyyMMdd" }
        "tomorrow" { return (Get-Date).AddDays(1) -Format "yyyyMMdd" }
    }
    
    if ($InputDate -match '^([+-])(\d+)d$') {
        $days = [int]$matches[2]
        if ($matches[1] -eq '-') { $days = -$days }
        return (Get-Date).AddDays($days) -Format "yyyyMMdd"
    }
    
    if ($InputDate -match '^\d{4}-\d{2}-\d{2}$') {
        return $InputDate.Replace('-', '')
    }
    
    if ($InputDate -match '^\d{8}$') {
        return $InputDate
    }
    
    throw "无效日期格式: $InputDate"
}

function Filter-By-LogType {
    param(
        [string]$FileName,
        [string]$LogType
    )
    
    switch ($LogType) {
        "error" { return $FileName.ToLower().Contains("-error") }
        "normal" { return -not $FileName.ToLower().Contains("-error") }
        default { return $true }
    }
}

$targetDate = Convert-Date $Date
$logDir = ".\logs"

Write-Host "查询: traceId=$TraceId, 日期=$targetDate, 日志类型=$LogType" -ForegroundColor Cyan

$files = Get-ChildItem -Path "$logDir\*$targetDate*.log" -File

if (-not $files) {
    Write-Warning "未找到日志文件: $logDir\*$targetDate*.log"
    return
}

$filteredFiles = $files | Where-Object { Filter-By-LogType -FileName $_.Name -LogType $LogType }

if (-not $filteredFiles) {
    Write-Warning "未找到匹配日志类型 '$LogType' 的日志文件"
    return
}

foreach ($file in $filteredFiles) {
    Write-Host "`n【文件】$($file.Name)" -ForegroundColor Green
    Write-Host "【路径】$($file.FullName)" -ForegroundColor Gray
    $result = Get-Content $file.FullName -ReadCount 1 | Select-String $TraceId -Context 2,10
    
    if ($result) {
        $result | ForEach-Object {
            if ($_.Context.PreContext) {
                Write-Host "`n--- 前置上下文 ---" -ForegroundColor DarkGray
                $_.Context.PreContext | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
            }
            Write-Host "`n$($_.LineNumber): $($_.Line)" -ForegroundColor White
            if ($_.Context.PostContext) {
                Write-Host "`n--- 后置上下文 ---" -ForegroundColor DarkGray
                $_.Context.PostContext | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
            }
        }
    } else {
        Write-Host "  (未找到匹配记录)" -ForegroundColor Yellow
    }
}

Write-Host "`n查询完成，共搜索 $($filteredFiles.Count) 个文件" -ForegroundColor Cyan