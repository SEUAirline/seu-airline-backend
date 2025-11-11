# ==============================================
# RabbitMQ Order Queue Stress Test (100 messages)
# ==============================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

# -------------------------
# Configuration
# -------------------------
$hostUrl = "http://localhost:8080"
$loginUrl = "$hostUrl/api/auth/login"
$orderUrl = "$hostUrl/api/rabbitmq/order"
$username = "user"
$password = "user123"
$totalMessages = 100

Write-Output "=== RabbitMQ Order Queue Stress Test (100 Messages) ==="
Write-Output ""

# -------------------------
# Step 1: Login
# -------------------------
Write-Output "-- Step 1: Logging in --"
$loginBody = @{ username = $username; password = $password } | ConvertTo-Json -Compress
try {
    $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    $token = $loginResponse.data.token
    if ([string]::IsNullOrEmpty($token)) {
        Write-Output "❌ Token not found in login response."
        exit 1
    }
    Write-Output "✅ Login successful! Token obtained."
} catch {
    Write-Output "❌ Login failed: $($_.Exception.Message)"
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}

# -------------------------
# Step 2: Send 100 Messages (Producer)
# -------------------------
Write-Output "`n-- Step 2: Sending $totalMessages order messages to RabbitMQ --"
Write-Output "(Producer → sends messages into order.queue)"
$successCount = 0
$failCount = 0

for ($i = 1; $i -le $totalMessages; $i++) {
    $flightNum = "TEST_FLIGHT_{0:D3}" -f $i
    $randomId = Get-Random -Minimum 1000 -Maximum 9999

    $orderBody = @{
        flightNumber = $flightNum
        userId = "5"
        items = @(
            @{
                passengerName = "Passenger_$randomId"
                seatId = (100 + $i)
                passengerIdCard = "3201011990$randomId"
            }
        )
    } | ConvertTo-Json -Depth 5

    try {
        $response = Invoke-RestMethod -Uri $orderUrl -Method Post -Headers $headers -Body $orderBody -ContentType "application/json" -ErrorAction Stop
        $successCount++
        Write-Host ("[$i/$totalMessages] ✅ Sent order: $flightNum") -ForegroundColor Green
    } catch {
        $failCount++
        Write-Host ("[$i/$totalMessages] ❌ Failed to send order: $flightNum - $($_.Exception.Message)") -ForegroundColor Red
    }

    Start-Sleep -Milliseconds 100  # optional delay (0.1s) to avoid server overload
}

Write-Output "`nSummary:"
Write-Output "✅ Successfully sent: $successCount"
Write-Output "❌ Failed: $failCount"

# -------------------------
# Step 3: Check Queue Status (Consumer Side)
# -------------------------
Write-Output "`n-- Step 3: Checking RabbitMQ queue status --"
Write-Output "(Consumer ← receives messages from order.queue)"

try {
    $queues = & rabbitmqctl list_queues name messages_ready messages_unacknowledged
    Write-Output "`nQueue status:"
    $queues | ForEach-Object { Write-Output $_ }
    Write-Output "`n✅ RabbitMQ 100-message stress test completed successfully!"
} catch {
    Write-Output "⚠️ Unable to check RabbitMQ queue status. Make sure rabbitmqctl is installed and accessible."
}

Write-Output "`n=== Test completed ==="
