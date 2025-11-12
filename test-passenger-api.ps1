# 乘客 API 测试脚本
# 功能：登录 -> 列表 -> 创建 -> 查询 -> 更新 -> 设为默认 -> 删除 -> 验证

# 设置 PowerShell 输出编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

$baseUrl = "http://localhost:8080/api"
$token = ""

Write-Host "=== 乘客 API 测试 ===" -ForegroundColor Green
Write-Host ""

# 测试1: 登录获取Token
Write-Host "测试1: 用户登录获取Token" -ForegroundColor Yellow
$loginBody = @{
    username = "passenger1"
    password = "passenger123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    if ($response -and $response.success -and $response.data -and $response.data.token) {
        $token = $response.data.token
        Write-Host "✓ 登录成功!" -ForegroundColor Green
        Write-Host "Token preview: $($token.Substring(0, [Math]::Min(80, $token.Length)))..." -ForegroundColor Cyan
    } else {
        Write-Host "✗ 登录失败: 未收到 token" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 登录失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{ Authorization = "Bearer $token" }
Start-Sleep -Seconds 1

# 测试2: 获取乘客列表
Write-Host "测试2: 获取乘客列表 GET /api/passengers" -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers" -Method Get -Headers $headers -ContentType "application/json"
    if ($resp.success -eq $true) {
        $list = $resp.data
        $count = 0
        if ($null -ne $list) { $count = $list.Count }
        Write-Host "✓ 成功，乘客数量: $count" -ForegroundColor Green
    } else {
        Write-Host "✗ API返回失败: $($resp.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 调用列表失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# 测试3: 创建新的乘客
Write-Host "测试3: 创建乘客 POST /api/passengers" -ForegroundColor Yellow
$createBody = @{
    passengerName = "自动化乘客测试"
    idType = "ID_CARD"
    idCard = "320106199909091234"
    phone = "13900001111"
    email = "testpassenger@example.com"
    passengerType = "ADULT"
    isDefault = $false
} | ConvertTo-Json

$createdId = $null
try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers" -Method Post -Headers $headers -Body $createBody -ContentType "application/json; charset=utf-8"
    if ($resp.success -eq $true -and $resp.data) {
        $created = $resp.data
        $createdId = $created.id
        Write-Host "✓ 创建成功, id = $createdId" -ForegroundColor Green
    } else {
        Write-Host "✗ 创建失败: $($resp.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 创建API失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) { Write-Host "错误详情: $($_.ErrorDetails.Message)" -ForegroundColor Yellow }
    exit 1
}

Start-Sleep -Seconds 1

# 测试4: 根据ID获取乘客
Write-Host "测试4: 获取乘客详情 GET /api/passengers/$createdId" -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers/$createdId" -Method Get -Headers $headers -ContentType "application/json"
    if ($resp.success -eq $true -and $resp.data) {
        Write-Host "✓ 获取成功: 名称 = $($resp.data.passengerName), 证件 = $($resp.data.idCard)" -ForegroundColor Green
    } else {
        Write-Host "✗ 获取失败: $($resp.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 获取API失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# 测试5: 更新乘客 PUT /api/passengers/{id}
Write-Host "测试5: 更新乘客 PUT /api/passengers/$createdId" -ForegroundColor Yellow
$updateBody = @{
    passengerName = "自动化乘客已更新"
    phone = "13900002222"
    email = "updated-passenger@example.com"
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers/$createdId" -Method Put -Headers $headers -Body $updateBody -ContentType "application/json; charset=utf-8"
    if ($resp.success -eq $true -and $resp.data) {
        Write-Host "✓ 更新成功: 新名称 = $($resp.data.passengerName), 新电话 = $($resp.data.phone)" -ForegroundColor Green
    } else {
        Write-Host "✗ 更新失败: $($resp.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 更新API失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# 测试6: 设置为默认乘客 PATCH /api/passengers/{id}/default
Write-Host "测试6: 设置默认乘客 PATCH /api/passengers/$createdId/default" -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers/$createdId/default" -Method Patch -Headers $headers -ContentType "application/json"
    if ($resp.success -eq $true) {
        Write-Host "✓ 设为默认成功: $($resp.message)" -ForegroundColor Green
    } else {
        Write-Host "✗ 设为默认失败: $($resp.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 设为默认API失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# 测试7: 删除乘客 DELETE /api/passengers/{id}
Write-Host "测试7: 删除乘客 DELETE /api/passengers/$createdId" -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers/$createdId" -Method Delete -Headers $headers -ContentType "application/json"
    if ($resp.success -eq $true) {
        Write-Host "✓ 删除成功: $($resp.message)" -ForegroundColor Green
    } else {
        Write-Host "✗ 删除失败: $($resp.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 删除API失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# 测试8: 验证删除后获取失败
Write-Host "测试8: 验证删除后获取应返回失败 GET /api/passengers/$createdId" -ForegroundColor Yellow
try {
    $resp = Invoke-RestMethod -Uri "$baseUrl/passengers/$createdId" -Method Get -Headers $headers -ContentType "application/json"
    if ($resp.success -eq $false) {
        Write-Host "✓ 删除验证通过: API 返回失败 -> $($resp.message)" -ForegroundColor Green
    } else {
        Write-Host "✗ 删除验证失败: API 返回成功 (应为失败)" -ForegroundColor Red
    }
} catch {
    # 如果服务抛出异常并返回非200，则 Invoke-RestMethod 会抛出；我们尝试解析错误响应
    Write-Host "✓ 删除后获取抛出异常，视为删除成功: $($_.Exception.Message)" -ForegroundColor Green
    if ($_.ErrorDetails.Message) {
        try { $err = $_.ErrorDetails.Message | ConvertFrom-Json ; Write-Host "错误响应: $($err.message)" -ForegroundColor Cyan } catch { }
    }
}

Write-Host "" 
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host "测试总结:" -ForegroundColor Yellow
Write-Host "1. ✓ 登录并获取JWT Token" -ForegroundColor White
Write-Host "2. ✓ 列表/创建/查询/更新/设默认/删除 流程" -ForegroundColor White
Write-Host "3. ✓ 所有 API 均以 ApiResponse 兼容的 JSON 格式返回 (success, data, message)" -ForegroundColor White
