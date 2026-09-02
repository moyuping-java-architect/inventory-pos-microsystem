$baseDir = "E:\spring boot\psi-modular"
$initFile = Join-Path $baseDir "docker\mysql\init.sql"

function Remove-UseDatabase($content) {
    $lines = $content -split "`n"
    $result = @()
    foreach ($line in $lines) {
        $trimmed = $line.Trim()
        if ($trimmed -match "^USE\s+\w+\s*;?$") {
            continue
        }
        $result += $line
    }
    return ($result -join "`n")
}

function Append-SqlFile($file, $sectionTitle) {
    $content = Get-Content -Path $file.FullName -Encoding UTF8 -Raw
    $content = Remove-UseDatabase $content
    $header = "`n`n-- ============================================`n-- $($sectionTitle)`n-- 来源: $($file.FullName.Replace($baseDir, ''))`n-- ============================================`n`n"
    $header + $content | Out-File -FilePath $initFile -Append -Encoding UTF8
    Write-Host "  appended: $($file.FullName.Replace($baseDir, ''))"
}

# 重置 init.sql
$header = @"
-- ============================================
-- psi-modular 单体数据库初始化脚本
-- 自动合并自各模块的 schema.sql 和 Flyway migration 脚本
-- 所有模块统一使用 psi_modular 数据库
-- ============================================

CREATE DATABASE IF NOT EXISTS psi_modular
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE psi_modular;
"@
$header | Out-File -FilePath $initFile -Encoding UTF8
Write-Host "init.sql header reset."

# 1. 合并 schema.sql
$schemaFiles = @()
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-common-starter-order-rule\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-member\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-finance\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-flow\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-goods\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-purchase\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles += Get-ChildItem -Path "$baseDir\psi-sale\src\main\resources\schema.sql" -ErrorAction SilentlyContinue
$schemaFiles = $schemaFiles | Where-Object { $_ -ne $null } | Sort-Object FullName

foreach ($schemaFile in $schemaFiles) {
    $module = ($schemaFile.FullName -split '\\src\\')[0] -replace '^.*\\', ''
    Append-SqlFile $schemaFile "$module schema.sql"
}

# 2. 合并 Flyway migration 脚本
$migrationDirs = @(
    "psi-common-starter-order-rule;$baseDir\psi-common-starter-order-rule\src\main\resources\migration"
    "psi-system;$baseDir\psi-system\src\main\resources\db\migration"
    "psi-message;$baseDir\psi-message\src\main\resources\db\migration"
    "psi-goods;$baseDir\psi-goods\src\main\resources\DB\migration"
    "psi-purchase;$baseDir\psi-purchase\src\main\resources\DB\migration"
    "psi-sale;$baseDir\psi-sale\src\main\resources\DB\migration"
    "psi-sync;$baseDir\psi-sync\src\main\resources\DB\migration"
)

foreach ($entry in $migrationDirs) {
    $parts = $entry -split ';', 2
    $module = $parts[0]
    $dir = $parts[1]
    if (Test-Path $dir) {
        $files = Get-ChildItem -Path $dir -Filter "V*.sql" | Where-Object {
            $_.Name -ne "V1__Create_Database.sql" -and
            $_.Name -ne "V1__ADD_BARCODE.sql" -and
            $_.Name -ne "V2__ADD_ORDER_NO.sql" -and
            $_.Name -ne "V1__ADD_USD_PRICE_AND_TAX_INCLUSIVE.sql" -and
            $_.Name -ne "V2__ADD_ZAMBIA_FIELDS_TO_SKU_SALE_UNIT.sql" -and
            $_.Name -ne "V1__ADD_PURCHASE_ITEM_TAX_INCLUSIVE.sql" -and
            $_.Name -ne "V1__ADD_SALE_ITEM_TAX_INCLUSIVE.sql"
        } | Sort-Object Name
        foreach ($migrationFile in $files) {
            Append-SqlFile $migrationFile "$module - $($migrationFile.Name)"
        }
    }
}

$lineCount = (Get-Content $initFile).Count
Write-Host "`ninit.sql 合并完成: $initFile"
Write-Host "总行数: $lineCount"
