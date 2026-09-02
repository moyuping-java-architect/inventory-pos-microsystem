# PSI系统登录测试脚本

## 测试环境
- 基础URL: http://localhost:8081
- 登录接口: POST /psi/admin/login

## 测试用例

### 1. 正常登录测试
```bash
# 测试用户: admin / 123456
curl -X POST http://localhost:8081/psi/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 2. 错误密码测试
```bash
curl -X POST http://localhost:8081/psi/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

### 3. 用户不存在测试
```bash
curl -X POST http://localhost:8081/psi/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nonexistent",
    "password": "123456"
  }'
```

### 4. 缺少用户名测试
```bash
curl -X POST http://localhost:8081/psi/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "password": "123456"
  }'
```

### 5. 缺少密码测试
```bash
curl -X POST http://localhost:8081/psi/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin"
  }'
```

## 测试登录日志接口

### 6. 查询登录日志列表 (需要先登录获取token)
```bash
# 先登录获取token
TOKEN=$(curl -s -X POST http://localhost:8081/psi/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}' \
  | jq -r '.data.token')

# 使用token查询登录日志
curl -X GET http://localhost:8081/psi/admin/login-log/list \
  -H "Authorization: Bearer $TOKEN"
```

### 7. 查询单个登录日志
```bash
curl -X GET http://localhost:8081/psi/admin/login-log/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 8. 删除登录日志
```bash
curl -X DELETE http://localhost:8081/psi/admin/login-log/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 9. 批量删除登录日志
```bash
curl -X DELETE http://localhost:8081/psi/admin/login-log/batch \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

## 测试Token刷新

### 10. 刷新Token
```bash
curl -X POST http://localhost:8081/psi/admin/refresh-token \
  -H "Refresh-Token: $REFRESH_TOKEN"
```

## 测试登出

### 11. 登出
```bash
curl -X POST http://localhost:8081/psi/admin/logout \
  -H "Authorization: Bearer $TOKEN"
```

## PowerShell 测试脚本

```powershell
# 设置基础URL
$baseUrl = "http://localhost:8081"

# 测试1: 正常登录
Write-Host "测试1: 正常登录" -ForegroundColor Green
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/psi/admin/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username": "admin", "password": "123456"}'

Write-Host "登录响应:" -ForegroundColor Yellow
$loginResponse | ConvertTo-Json -Depth 10

# 提取token
$token = $loginResponse.data.token
Write-Host "获取到的Token: $token" -ForegroundColor Cyan

# 测试2: 查询登录日志
Write-Host "`n测试2: 查询登录日志" -ForegroundColor Green
$logResponse = Invoke-RestMethod -Uri "$baseUrl/psi/admin/login-log/list" `
  -Method GET `
  -Headers @{
    "Authorization" = "Bearer $token"
  }

Write-Host "登录日志列表:" -ForegroundColor Yellow
$logResponse | ConvertTo-Json -Depth 10

# 测试3: 错误密码
Write-Host "`n测试3: 错误密码" -ForegroundColor Green
try {
  $errorResponse = Invoke-RestMethod -Uri "$baseUrl/psi/admin/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username": "admin", "password": "wrongpassword"}'
  
  Write-Host "错误密码响应:" -ForegroundColor Yellow
  $errorResponse | ConvertTo-Json -Depth 10
} catch {
  Write-Host "错误密码测试失败 (预期行为): $_" -ForegroundColor Red
}

# 测试4: 用户不存在
Write-Host "`n测试4: 用户不存在" -ForegroundColor Green
try {
  $notFoundResponse = Invoke-RestMethod -Uri "$baseUrl/psi/admin/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username": "nonexistent", "password": "123456"}'
  
  Write-Host "用户不存在响应:" -ForegroundColor Yellow
  $notFoundResponse | ConvertTo-Json -Depth 10
} catch {
  Write-Host "用户不存在测试失败 (预期行为): $_" -ForegroundColor Red
}

Write-Host "`n测试完成!" -ForegroundColor Green
```

## 预期结果

### 正常登录响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "tenantId": 1,
      "shopId": 1,
      "warehouseId": 1,
      "roleId": 1,
      "roleName": "超级管理员",
      "permissions": "*:*:*"
    }
  }
}
```

### 错误响应
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

## 注意事项

1. 确保应用已启动在 http://localhost:8081
2. 数据库中已初始化测试数据 (admin/123456)
3. JWT token默认有效期为24小时
4. 所有需要认证的接口都需要在请求头中携带 `Authorization: Bearer {token}`
5. 登录日志接口需要先登录获取token才能访问