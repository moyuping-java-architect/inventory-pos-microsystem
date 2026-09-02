<#
.SYNOPSIS
TraceId 日志查询工具 - PowerShell 版本

.DESCRIPTION
根据 traceId 和日期快速查询日志文件，支持上下文显示

.PARAMETER TraceId
要查询的 traceId（必填）

.PARAMETER Date
查询日期，格式 yyyyMMdd，默认昨天

.PARAMETER LogDir
日志目录，默认 D:\logs

.PARAMETER AfterLines
匹配行后显示的行数，默认 10 行

.PARAMETER BeforeLines
匹配行前显示的行数，默认 2 行

.EXAMPLE
.\trace-query.ps1 -TraceId "abc123"
查询今天的日志，traceId 为 abc123

.EXAMPLE
.\trace-query.ps1 -TraceId "abc123" -Date "20251230"
查询 2025-12-30 的日志

.EXAMPLE
.\trace-query.ps1 -TraceId "abc123" -LogDir "E:\app\logs" -AfterLines 5
指定日志目录和上下文行数

.NOTES
使用 Select-String 进行高效搜索，支持大文件
#>

param(
    [Parameter(Mandatory=$true, HelpMessage="要查询的 traceId")]
    [string]$TraceId,
    
    [Parameter(HelpMessage="查询日期，格式 yyyyMMdd，默认昨天")]
    [string]$Date = (Get-Date -Format "yyyyMMdd"),
    
    [Parameter(HelpMessage="日志目录，默认 .\logs（相对路径）")]
    [string]$LogDir = ".\logs",
    
    [Parameter(HelpMessage="匹配行后显示的行数")]
    [int]$AfterLines = 10,
    
    [Parameter(HelpMessage="匹配行前显示的行数")]
    [string]$BeforeLines = 2
)

# 检查日志目录是否存在
if (-not (Test-Path -Path $LogDir -PathType Container)) {
    Write-Error "日志目录不存在: $LogDir"
    exit 1
}

# 构建文件匹配模式
$filePattern = "*$Date*.log"
$filePath = Join-Path -Path $LogDir -ChildPath $filePattern

# 查找日志文件
$logFiles = Get-ChildItem -Path $filePath -File -ErrorAction SilentlyContinue

if (-not $logFiles) {
    Write-Warning "未找到日期为 $Date 的日志文件"
    Write-Warning "搜索模式: $filePath"
    exit 0
}

Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "                    TraceId 日志查询工具                              " -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  查询参数:" -ForegroundColor Yellow
Write-Host "    traceId:    $TraceId"
Write-Host "    日期:       $Date"
Write-Host "    日志目录:   $LogDir"
Write-Host "    上下文行数: 前 $BeforeLines 行, 后 $AfterLines 行"
Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# 统计结果数量
$totalMatches = 0

# 遍历每个日志文件
foreach ($file in $logFiles) {
    Write-Host "┌────────────────────────────────────────────────────────────────────┐" -ForegroundColor Gray
    Write-Host "│ 文件: $($file.Name)" -ForegroundColor Gray
    Write-Host "│ 路径: $($file.FullName)" -ForegroundColor Gray
    Write-Host "└────────────────────────────────────────────────────────────────────┘" -ForegroundColor Gray
    
    # 使用 Get-Content 和 Select-String 高效搜索
    $matches = Get-Content -Path $file.FullName -ReadCount 1 | 
               Select-String -Pattern $TraceId -Context $BeforeLines,$AfterLines
    
    if ($matches) {
        $totalMatches += $matches.Count
        
        foreach ($match in $matches) {
            Write-Host ""
            Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Green
            Write-Host "  匹配位置: 第 $($match.LineNumber) 行" -ForegroundColor Green
            
            # 显示前置上下文
            if ($match.Context.PreContext) {
                Write-Host ""
                Write-Host "  ── 前置上下文 ──" -ForegroundColor DarkGray
                $preIndex = $match.LineNumber - $match.Context.PreContext.Count
                foreach ($preLine in $match.Context.PreContext) {
                    Write-Host "    [$preIndex] $preLine" -ForegroundColor Gray
                    $preIndex++
                }
            }
            
            # 显示匹配行
            Write-Host ""
            Write-Host "  ── 匹配行 ──" -ForegroundColor DarkGray
            Write-Host "    [$($match.LineNumber)] $($match.Line)" -ForegroundColor Yellow
            
            # 显示后置上下文
            if ($match.Context.PostContext) {
                Write-Host ""
                Write-Host "  ── 后置上下文 ──" -ForegroundColor DarkGray
                $postIndex = $match.LineNumber + 1
                foreach ($postLine in $match.Context.PostContext) {
                    Write-Host "    [$postIndex] $postLine" -ForegroundColor Gray
                    $postIndex++
                }
            }
            
            Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Green
        }
    } else {
        Write-Host "    无匹配记录" -ForegroundColor Gray
    }
    
    Write-Host ""
}

Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  查询完成" -ForegroundColor Cyan
Write-Host "    搜索文件数: $($logFiles.Count)" -ForegroundColor Yellow
Write-Host "    匹配记录数: $totalMatches" -ForegroundColor Yellow
Write-Host "══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan