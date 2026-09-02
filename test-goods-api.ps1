# Test Goods Management APIs
$baseUrl = "http://localhost:8080"

Write-Host "========================================"
Write-Host "    Goods Management API Test"
Write-Host "========================================"
Write-Host ""

# Test Category API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/category/page?pageNum=1&pageSize=10" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] Category Page Query"
    } else {
        Write-Host "[FAIL] Category Page Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] Category Page Query: $($_.Exception.Message)"
}

# Test Category Tree API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/category/tree" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] Category Tree Query"
    } else {
        Write-Host "[FAIL] Category Tree Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] Category Tree Query: $($_.Exception.Message)"
}

# Test Brand API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/brand/page?pageNum=1&pageSize=10" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] Brand Page Query"
    } else {
        Write-Host "[FAIL] Brand Page Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] Brand Page Query: $($_.Exception.Message)"
}

# Test Unit API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/unit/page?pageNum=1&pageSize=10" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] Unit Page Query"
    } else {
        Write-Host "[FAIL] Unit Page Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] Unit Page Query: $($_.Exception.Message)"
}

# Test Goods List API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/list?pageNum=1&pageSize=10" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] Goods List Query"
    } else {
        Write-Host "[FAIL] Goods List Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] Goods List Query: $($_.Exception.Message)"
}

# Test SKU API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/sku/page?pageNum=1&pageSize=10" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] SKU Page Query"
    } else {
        Write-Host "[FAIL] SKU Page Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] SKU Page Query: $($_.Exception.Message)"
}

# Test SKU Sale Unit API
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/psi/goods/sku/sale-unit/page?pageNum=1&pageSize=10" -Method GET -ContentType "application/json" -TimeoutSec 10
    if ($response.code -eq 200) {
        Write-Host "[OK] SKU Sale Unit Page Query"
    } else {
        Write-Host "[FAIL] SKU Sale Unit Page Query: $($response.message)"
    }
} catch {
    Write-Host "[ERROR] SKU Sale Unit Page Query: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "========================================"
Write-Host "    Test Completed"
Write-Host "========================================"