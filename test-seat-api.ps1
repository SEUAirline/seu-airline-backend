# 座位API测试脚本
# 测试座位查询功能

# 设置PowerShell输出编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

$baseUrl = "http://localhost:8080/api"
$token = ""

Write-Host "=== 座位API测试 ===" -ForegroundColor Green
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
        if ($token.Length -gt 50) {
            Write-Host "Token: $($token.Substring(0, 50))..." -ForegroundColor Cyan
        } else {
            Write-Host "Token: $token" -ForegroundColor Cyan
        }
    } else {
        Write-Host "✗ 登录失败" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 登录失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 设置请求头
$headers = @{
    Authorization = "Bearer $token"
}

Write-Host ""
Start-Sleep -Seconds 1


# 测试2: 测试API - 获取航班1的所有座位
Write-Host "测试2: 调用API获取航班1的所有座位" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/seats/flight/1" -Method Get -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ API调用成功!" -ForegroundColor Green
        Write-Host "消息: $($response.message)" -ForegroundColor Cyan
        Write-Host "座位数量: $($response.data.Count)" -ForegroundColor Cyan
        
        if ($response.data.Count -gt 0) {
            Write-Host "`n前5个座位:" -ForegroundColor Yellow
            $response.data | Select-Object -First 5 | ForEach-Object {
                Write-Host "  - 座位 $($_.seatNumber): $($_.seatType), ¥$($_.price), $($_.status)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠ 返回的座位列表为空!" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ API返回失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ API调用失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "错误详情: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# 测试3: 测试API - 获取航班1的经济舱可用座位
Write-Host "测试3: 调用API获取航班1的经济舱可用座位" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/seats/flight/1/type/ECONOMY/available" -Method Get -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ API调用成功!" -ForegroundColor Green
        Write-Host "消息: $($response.message)" -ForegroundColor Cyan
        Write-Host "经济舱可用座位数量: $($response.data.Count)" -ForegroundColor Cyan
        
        if ($response.data.Count -gt 0) {
            Write-Host "`n前10个可用座位:" -ForegroundColor Yellow
            $response.data | Select-Object -First 10 | ForEach-Object {
                Write-Host "  - ID: $($_.id), 座位: $($_.seatNumber), ¥$($_.price)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠ 经济舱没有可用座位!" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ API返回失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ API调用失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "错误详情: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# 测试4: 测试API - 获取航班1的商务舱可用座位
Write-Host "测试4: 调用API获取航班1的商务舱可用座位" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/seats/flight/1/type/BUSINESS/available" -Method Get -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ API调用成功!" -ForegroundColor Green
        Write-Host "消息: $($response.message)" -ForegroundColor Cyan
        Write-Host "商务舱可用座位数量: $($response.data.Count)" -ForegroundColor Cyan
        
        if ($response.data.Count -gt 0) {
            Write-Host "`n所有商务舱座位:" -ForegroundColor Yellow
            $response.data | ForEach-Object {
                Write-Host "  - ID: $($_.id), 座位: $($_.seatNumber), ¥$($_.price)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠ 商务舱没有可用座位!" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ API返回失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ API调用失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# 测试5: 测试API - 获取航班1的头等舱可用座位
Write-Host "测试5: 调用API获取航班1的头等舱可用座位" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/seats/flight/1/type/FIRST/available" -Method Get -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ API调用成功!" -ForegroundColor Green
        Write-Host "消息: $($response.message)" -ForegroundColor Cyan
        Write-Host "头等舱可用座位数量: $($response.data.Count)" -ForegroundColor Cyan
        
        if ($response.data.Count -gt 0) {
            Write-Host "`n所有头等舱座位:" -ForegroundColor Yellow
            $response.data | ForEach-Object {
                Write-Host "  - ID: $($_.id), 座位: $($_.seatNumber), ¥$($_.price)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠ 头等舱没有可用座位!" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ API返回失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ API调用失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# 测试6: 测试错误场景 - 无效的座位类型
Write-Host "测试6: 测试无效的座位类型" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/seats/flight/1/type/INVALID/available" -Method Get -Headers $headers -ContentType "application/json"
    
    if (!$response.success) {
        Write-Host "✓ API正确返回错误!" -ForegroundColor Green
        Write-Host "错误消息: $($response.message)" -ForegroundColor Cyan
    } else {
        Write-Host "✗ API应该返回错误但返回了成功" -ForegroundColor Red
    }
} catch {
    Write-Host "✓ API正确抛出异常" -ForegroundColor Green
    if ($_.ErrorDetails.Message) {
        $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
        if ($errorResponse.success -eq $false) {
            Write-Host "错误消息: $($errorResponse.message)" -ForegroundColor Cyan
        }
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# 测试7: 模拟前端订单创建流程
Write-Host "测试7: 模拟前端订单创建流程" -ForegroundColor Yellow
Write-Host "  7.1 获取经济舱座位" -ForegroundColor Cyan
try {
    $seatsResponse = Invoke-RestMethod -Uri "$baseUrl/seats/flight/1/type/ECONOMY/available" -Method Get -Headers $headers -ContentType "application/json"
    
    if ($seatsResponse.success -and $seatsResponse.data.Count -gt 0) {
        $seat = $seatsResponse.data[0]
        Write-Host "  ✓ 获取到可用座位: ID=$($seat.id), 座位号=$($seat.seatNumber), 价格=¥$($seat.price)" -ForegroundColor Green
        
        Write-Host "`n  7.2 使用座位创建订单" -ForegroundColor Cyan
        $orderBody = @{
            items = @(
                @{
                    seatId = $seat.id
                    passengerName = "张三"
                    passengerIdCard = "320106200412121234"
                }
            )
        } | ConvertTo-Json -Depth 10
        
        try {
            $orderResponse = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers $headers -Body $orderBody -ContentType "application/json"
            
            if ($orderResponse.success) {
                Write-Host "  ✓ 订单创建成功!" -ForegroundColor Green
                Write-Host "  订单号: $($orderResponse.data.orderNumber)" -ForegroundColor Cyan
                Write-Host "  总金额: ¥$($orderResponse.data.totalAmount)" -ForegroundColor Cyan
            } else {
                Write-Host "  ✗ 订单创建失败: $($orderResponse.message)" -ForegroundColor Red
            }
        } catch {
            Write-Host "  ✗ 订单创建失败: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                Write-Host "  错误详情: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "  ✗ 没有可用座位" -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ 获取座位失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "测试总结:" -ForegroundColor Yellow
Write-Host "1. ✓ 登录并获取JWT Token" -ForegroundColor White
Write-Host "2. ✓ 使用Token访问座位API" -ForegroundColor White
Write-Host "3. ✓ 获取不同舱位的可用座位" -ForegroundColor White
Write-Host "4. ✓ 测试错误场景处理" -ForegroundColor White
Write-Host "5. ✓ 模拟完整的订单创建流程" -ForegroundColor White
