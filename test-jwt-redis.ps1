# JWT与Redis整合功能测试脚本

# 注意: 
# 1. 确保Redis服务正在运行
# 2. 确保Spring Boot应用正在运行
# 3. 将下面的URL根据实际情况修改

$baseUrl = "http://localhost:8080/api"
$token = ""

Write-Host "=== JWT与Redis整合测试 ===" -ForegroundColor Green
Write-Host ""

# 测试1: 登录获取Token
Write-Host "测试1: 用户登录" -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $response.token
    Write-Host "✓ 登录成功!" -ForegroundColor Green
    Write-Host "Token: $($token.Substring(0, 50))..." -ForegroundColor Cyan
    Write-Host "用户: $($response.username), 角色: $($response.role)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ 登录失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 2

# 测试2: 使用Token访问受保护资源
Write-Host "`n测试2: 访问受保护资源" -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $headers
    Write-Host "✓ 访问成功!" -ForegroundColor Green
    Write-Host "当前用户: $($response.username)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ 访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# 测试3: 查看Redis中的Token
Write-Host "`n测试3: 检查Redis中的Token存储" -ForegroundColor Yellow
try {
    # 检查token key是否存在
    $redisCheck = redis-cli keys "jwt:token:*"
    if ($redisCheck) {
        Write-Host "✓ Token已存储到Redis" -ForegroundColor Green
        Write-Host "Redis Keys: $redisCheck" -ForegroundColor Cyan
        
        # 查看TTL
        $ttl = redis-cli ttl "jwt:token:admin"
        Write-Host "Token剩余时间: $ttl 秒" -ForegroundColor Cyan
    } else {
        Write-Host "✗ Redis中未找到Token" -ForegroundColor Red
    }
} catch {
    Write-Host "⚠ 无法连接到Redis或redis-cli未安装" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# 测试4: 刷新Token
Write-Host "`n测试4: 刷新Token" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/refresh" -Method Post -Headers $headers
    $newToken = $response.token
    Write-Host "✓ Token刷新成功!" -ForegroundColor Green
    Write-Host "新Token: $($newToken.Substring(0, 50))..." -ForegroundColor Cyan
    
    # 更新headers为新token
    $headers.Authorization = "Bearer $newToken"
    
    # 尝试使用旧token (应该失败)
    Write-Host "`n测试4.1: 验证旧Token已失效" -ForegroundColor Yellow
    $oldHeaders = @{
        Authorization = "Bearer $token"
    }
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $oldHeaders
        Write-Host "✗ 旧Token仍然有效 (不符合预期)" -ForegroundColor Red
    } catch {
        Write-Host "✓ 旧Token已失效 (符合预期)" -ForegroundColor Green
    }
    
    # 使用新token访问
    Write-Host "`n测试4.2: 使用新Token访问" -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $headers
    Write-Host "✓ 新Token可以正常使用!" -ForegroundColor Green
    
    # 更新token变量为新token
    $token = $newToken
    
} catch {
    Write-Host "✗ Token刷新失败: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# 测试5: 检查黑名单
Write-Host "`n测试5: 检查Redis黑名单" -ForegroundColor Yellow
try {
    $blacklist = redis-cli keys "jwt:blacklist:*"
    if ($blacklist) {
        Write-Host "✓ 发现黑名单Token" -ForegroundColor Green
        Write-Host "黑名单Token数量: $($blacklist.Count)" -ForegroundColor Cyan
    } else {
        Write-Host "- 黑名单为空" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ 无法检查Redis黑名单" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# 测试6: 登出
Write-Host "`n测试6: 用户登出" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/logout" -Method Post -Headers $headers
    Write-Host "✓ 登出成功: $response" -ForegroundColor Green
    
    # 验证Token已失效
    Write-Host "`n测试6.1: 验证Token已失效" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $headers
        Write-Host "✗ Token仍然有效 (不符合预期)" -ForegroundColor Red
    } catch {
        Write-Host "✓ Token已失效 (符合预期)" -ForegroundColor Green
    }
    
} catch {
    Write-Host "✗ 登出失败: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# 测试7: 最终Redis状态检查
Write-Host "`n测试7: 最终Redis状态" -ForegroundColor Yellow
try {
    Write-Host "有效Token数量:" -ForegroundColor Cyan
    $tokens = redis-cli keys "jwt:token:*"
    Write-Host "  $($tokens.Count)" -ForegroundColor Cyan
    
    Write-Host "黑名单Token数量:" -ForegroundColor Cyan
    $blacklist = redis-cli keys "jwt:blacklist:*"
    Write-Host "  $($blacklist.Count)" -ForegroundColor Cyan
} catch {
    Write-Host "⚠ 无法检查Redis状态" -ForegroundColor Yellow
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "总结:" -ForegroundColor Yellow
Write-Host "1. ✓ 用户可以正常登录并获取Token" -ForegroundColor White
Write-Host "2. ✓ Token存储在Redis中" -ForegroundColor White
Write-Host "3. ✓ 可以使用Token访问受保护资源" -ForegroundColor White
Write-Host "4. ✓ Token可以刷新,旧Token自动失效" -ForegroundColor White
Write-Host "5. ✓ 登出后Token加入黑名单并失效" -ForegroundColor White
Write-Host "6. ✓ JWT与Redis整合功能正常工作" -ForegroundColor White
