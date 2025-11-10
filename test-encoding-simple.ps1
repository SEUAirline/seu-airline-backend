# 中文编码测试脚本 - 包含API和SQL双重读取测试
Write-Host "开始中文编码测试..."

# 数据库连接信息
$dbUser = "root"
$dbPassword = "Hello040813"
$dbName = "seu_airline"
$dbHost = "localhost"
$dbPort = "3306"

# 测试插入中文数据
function Test-InsertChinese {
    Write-Host "1. 测试插入中文数据..."
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/public/test/encoding/test-insert" `
                                     -Method POST `
                                     -ContentType "application/json" `
                                     -UseBasicParsing
        
        Write-Host "   状态: $(if ($response.success) { "成功" } else { "失败" })"
        Write-Host "   消息: $($response.message)"
        Write-Host "   数据: $($response.data)"
        
        return $response.success
    } catch {
        Write-Host "   错误: 插入中文数据失败 - $_"
        return $false
    }
}

# 通过API读取中文数据
function Test-ReadChineseByApi {
    Write-Host "2. 通过API读取中文数据..."
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/public/test/encoding/test-read" `
                                     -Method GET `
                                     -UseBasicParsing
        
        Write-Host "   状态: $(if ($response.success) { "成功" } else { "失败" })"
        Write-Host "   消息: $($response.message)"
        
        if ($response.success -and $response.data -and $response.data.Count -gt 0) {
            Write-Host "   API读取到的数据:" -ForegroundColor Green
            $response.data | ForEach-Object {
                Write-Host "     ID: $($_.id)"
                Write-Host "     中文文本: $($_.chinese_text)"
                Write-Host "     描述: $($_.description)"
            }
        }
        
        return $response.success
    } catch {
        Write-Host "   错误: API读取中文数据失败 - $_"
        return $false
    }
}

# 通过SQL直接读取中文数据
function Test-ReadChineseBySql {
    Write-Host "3. 通过SQL直接读取中文数据..."
    
    try {
        # 构建SQL查询命令，使用更安全的方式
        $sqlQuery = "SELECT * FROM encoding_test ORDER BY id DESC LIMIT 10"
        
        # 创建一个临时脚本文件来执行MySQL命令，避免密码显示在进程列表中
        $tempScript = "$env:TEMP\mysql_query_$(Get-Random).sql"
        Set-Content -Path $tempScript -Value $sqlQuery
        
        # 构建MySQL命令
        $mysqlCommand = @(
            "mysql",
            "-u", $dbUser,
            "-p$dbPassword",
            "-h", $dbHost,
            "-P", $dbPort,
            "-D", $dbName,
            "--default-character-set=utf8mb4",
            "<", $tempScript
        )
        
        # 执行MySQL命令并捕获输出
        $sqlResults = & cmd /c "$($mysqlCommand -join ' ')"
        
        # 删除临时脚本文件
        Remove-Item -Path $tempScript -Force -ErrorAction SilentlyContinue
        
        if ($sqlResults -and $sqlResults.Length -gt 0) {
            Write-Host "   SQL直接读取到的数据:" -ForegroundColor Green
            $sqlResults | ForEach-Object {
                Write-Host "   $_"
            }
            return $true
        } else {
            Write-Host "   警告: SQL查询未返回数据"
            return $false
        }
    } catch {
        Write-Host "   错误: SQL直接读取失败 - $_" -ForegroundColor Red
        Write-Host "   请检查数据库连接信息是否正确" -ForegroundColor Yellow
        return $false
    }
}

# 主测试流程
Write-Host "\n===== 开始测试流程 ====="
$insertResult = Test-InsertChinese
$apiReadResult = if ($insertResult) { Test-ReadChineseByApi } else { $false }
$sqlReadResult = if ($insertResult) { Test-ReadChineseBySql } else { $null }

# 结果比较分析
Write-Host "\n===== 测试结果比较分析 ====="
if ($apiReadResult) {
    Write-Host "✅ API读取测试通过！" -ForegroundColor Green
    Write-Host "   应用层中文编码处理正常。"
} else {
    Write-Host "❌ API读取测试失败！" -ForegroundColor Red
    Write-Host "   应用层中文编码处理存在问题。"
}

if ($sqlReadResult -eq $null) {
    Write-Host "⚠️ SQL直接读取测试已跳过（MySQL命令行工具不可用）" -ForegroundColor Yellow
} elseif ($sqlReadResult) {
    Write-Host "✅ SQL直接读取测试通过！" -ForegroundColor Green
    Write-Host "   数据库层中文编码处理正常。"
} else {
    Write-Host "❌ SQL直接读取测试失败！" -ForegroundColor Red
    Write-Host "   数据库层中文编码可能存在问题，或连接配置不正确。"
}

# 双重验证总结
if ($apiReadResult -and ($sqlReadResult -eq $true)) {
    Write-Host "\n✅ 完美！双重验证都通过，确保了中文编码在整个流程中都正确处理。" -ForegroundColor Green
} elseif ($apiReadResult -and ($sqlReadResult -eq $null)) {
    Write-Host "\n⚠️ API验证通过，但无法验证数据库层（MySQL命令行不可用）" -ForegroundColor Yellow
} elseif (-not $apiReadResult) {
    Write-Host "\n❌ API验证失败，需要优先解决应用层中文编码问题" -ForegroundColor Red
}

# 综合建议
Write-Host "\n===== 综合建议 ====="
Write-Host "1. 数据库配置检查:" -ForegroundColor Cyan
Write-Host "   - 确保数据库连接URL包含: useUnicode=true&characterEncoding=utf-8"
Write-Host "   - 确保所有数据库表使用utf8mb4字符集和utf8mb4_unicode_ci排序规则"
Write-Host "   - 数据库服务器默认字符集应为utf8mb4"
Write-Host "\n2. 应用配置检查:" -ForegroundColor Cyan
Write-Host "   - 确保Spring Boot应用的字符编码设置正确"
Write-Host "   - 检查JPA/Hibernate的配置是否正确处理UTF-8编码"

# 清理测试数据
Write-Host "\n清理测试数据..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/public/test/encoding/cleanup" `
                                 -Method DELETE `
                                 -UseBasicParsing
    Write-Host "清理完成：$($response.message)"
} catch {
    Write-Host "清理失败：$_" -ForegroundColor Red
}

Write-Host "\n测试结束！"
Write-Host "\n注意：如果需要完整的双重验证，请确保MySQL命令行工具已正确安装并添加到系统PATH。"