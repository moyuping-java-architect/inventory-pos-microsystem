$files = @(
    "E:\spring boot\psi-modular\psi-purchase\src\main\resources\schema.sql"
    "E:\spring boot\psi-modular\psi-sale\src\main\resources\schema.sql"
)

foreach ($file in $files) {
    $content = Get-Content -Path $file -Encoding UTF8
    $fixed = 0
    for ($i = 0; $i -lt $content.Count; $i++) {
        $line = $content[$i]
        if ([string]::IsNullOrEmpty($line)) { continue }
        # 检查包含 COMMENT 且只有一个单引号的行（缺少闭合引号）
        if ($line.Contains("COMMENT") -and $line.Contains("'")) {
            $quoteCount = ($line.ToCharArray() | Where-Object { $_ -eq "'" }).Count
            if ($quoteCount -eq 1) {
                $trimmed = $line.TrimEnd()
                # 在最后一个非空白字符前插入闭合引号
                # 如果行尾是 , 或 ;，在它们之前插入 '
                if ($trimmed.EndsWith(",") -or $trimmed.EndsWith(";")) {
                    $content[$i] = $trimmed.Substring(0, $trimmed.Length - 1) + "'" + $trimmed.Substring($trimmed.Length - 1)
                    $fixed++
                } else {
                    $content[$i] = $trimmed + "'"
                    $fixed++
                }
            }
        }
    }
    if ($fixed -gt 0) {
        $content | Out-File -FilePath $file -Encoding UTF8
        Write-Host "Fixed $fixed lines in $file"
    } else {
        Write-Host "No fixes needed in $file"
    }
}
