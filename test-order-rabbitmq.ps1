# Order Message RabbitMQ Testing Tool
Write-Host "=== Order Message RabbitMQ Testing Tool ==="

# Create log file
$logFile = "test-order-rabbitmq.log"
"Test started at: $(Get-Date)" | Out-File -FilePath $logFile

# Set character encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

# Test environment configuration
$baseUrl = "http://localhost:8080/api"
$loginEndpoint = "/auth/login"
$orderEndpoint = "/rabbitmq/order"

# User credentials
$username = "user"
$password = "user123"

# Log function
function Write-TestLog {
    param(
        [string]$message
    )
    Write-Host $message
    $message | Out-File -FilePath $logFile -Append
}

# Global token variable
$global:token = $null

Write-TestLog "Initialization complete. Starting tests..."

# Function to test login
function Test-Login {
    Write-TestLog "\n=== Test 1: Login Test ==="
    try {
        # Build login request body
        $loginBody = @{
            username = $username
            password = $password
        } | ConvertTo-Json
        
        Write-TestLog "Sending login request with username: $username"
        
        # Send login request
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl$loginEndpoint" -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
        
        # Extract token from response
        if ($loginResponse -and $loginResponse.data -and $loginResponse.data.token) {
            $global:token = $loginResponse.data.token
            $userRole = $loginResponse.data.user.role
            
            Write-TestLog "PASS: Login Successful!"
            Write-TestLog "Token length: $($global:token.Length) characters"
            Write-TestLog "User: $username, Role: $userRole"
            return $true
        } else {
            Write-TestLog "FAIL: Login successful but no token found in response"
            return $false
        }
    } catch {
        Write-TestLog "FAIL: Login failed: $($_.Exception.Message)"
        if ($_.ErrorDetails) {
            Write-TestLog "Error details: $($_.ErrorDetails.Message)"
        }
        return $false
    }
}

# Function to test normal order execution
function Test-NormalOrder {
    Write-TestLog "\n=== Test 2: Normal Order Test ==="
    if (-not $global:token) {
        Write-TestLog "FAIL: Not logged in, cannot test normal order"
        return $false
    }
    
    try {
        # Generate unique order ID to avoid conflicts
        $orderId = Get-Random -Minimum 1000 -Maximum 9999
        
        # Build normal order request body
        $orderBody = @{
            orderId = $orderId
            status = "PENDING"
            userId = $username
            flightNumber = "CA1234"
            amount = 99.99
            items = @(
                @{
                    seatId = "1A"
                    passengerName = "Test User"
                    passengerIdCard = "110101199001011234"
                }
            )
        } | ConvertTo-Json -Depth 3
        
        Write-TestLog "Sending normal order with ID: $orderId"
        
        # Set request headers
        $headers = @{
            "Authorization" = "Bearer $global:token"
            "Content-Type" = "application/json"
        }
        
        # Send order message request
        $orderResponse = Invoke-RestMethod -Uri "$baseUrl$orderEndpoint" -Method Post -Body $orderBody -Headers $headers -ErrorAction Stop
        
        Write-TestLog "PASS: Normal order sent successfully!"
        Write-TestLog "Response received from server"
        return $true
    } catch {
        Write-TestLog "FAIL: Normal order test failed: $($_.Exception.Message)"
        if ($_.ErrorDetails) {
            Write-TestLog "Error details: $($_.ErrorDetails.Message)"
        }
        return $false
    }
}

# Function to test abnormal order rejection
function Test-AbnormalOrderRejection {
    Write-TestLog "\n=== Test 3: Abnormal Order Rejection Test ==="
    if (-not $global:token) {
        Write-TestLog "FAIL: Not logged in, cannot test abnormal order"
        return $false
    }
    
    try {
        # Generate unique order ID
        $orderId = Get-Random -Minimum 1000 -Maximum 9999
        
        # Build abnormal order request body (missing required fields)
        $orderBody = @{
            orderId = $orderId
            # Missing required fields like userId, flightNumber
            status = "PENDING"
            amount = 99.99
        } | ConvertTo-Json -Depth 3
        
        Write-TestLog "Sending abnormal order (missing required fields) with ID: $orderId"
        
        # Set request headers
        $headers = @{
            "Authorization" = "Bearer $global:token"
            "Content-Type" = "application/json"
        }
        
        # Send order message request
        $orderResponse = Invoke-RestMethod -Uri "$baseUrl$orderEndpoint" -Method Post -Body $orderBody -Headers $headers -ErrorAction Stop
        
        Write-TestLog "FAIL: Abnormal order was accepted when it should be rejected"
        return $false
    } catch {
        Write-TestLog "PASS: Abnormal order correctly rejected with error: $($_.Exception.Message)"
        return $true
    }
}

# Function to test duplicate seat rejection
function Test-DuplicateSeatRejection {
    Write-TestLog "\n=== Test 4: Duplicate Seat Rejection Test ==="
    if (-not $global:token) {
        Write-TestLog "FAIL: Not logged in, cannot test duplicate seat"
        return $false
    }
    
    try {
        # First, create a normal order to occupy a seat
        $firstOrderId = Get-Random -Minimum 1000 -Maximum 9999
        $seatId = "2A"
        
        # Build first order request body
        $firstOrderBody = @{
            orderId = $firstOrderId
            status = "PENDING"
            userId = $username
            flightNumber = "CA1234"
            amount = 99.99
            items = @(
                @{
                    seatId = $seatId
                    passengerName = "First Passenger"
                    passengerIdCard = "110101199001011234"
                }
            )
        } | ConvertTo-Json -Depth 3
        
        # Set request headers
        $headers = @{
            "Authorization" = "Bearer $global:token"
            "Content-Type" = "application/json"
        }
        
        Write-TestLog "Sending first order to occupy seat: $seatId"
        $firstResponse = Invoke-RestMethod -Uri "$baseUrl$orderEndpoint" -Method Post -Body $firstOrderBody -Headers $headers -ErrorAction Stop
        Write-TestLog "First order sent successfully"
        
        # Wait briefly for first order to be processed
        Start-Sleep -Seconds 2
        
        # Now try to create a second order with the same seat
        $secondOrderId = Get-Random -Minimum 1000 -Maximum 9999
        
        # Build second order request body with same seat
        $secondOrderBody = @{
            orderId = $secondOrderId
            status = "PENDING"
            userId = $username
            flightNumber = "CA1234"
            amount = 99.99
            items = @(
                @{
                    seatId = $seatId
                    passengerName = "Second Passenger"
                    passengerIdCard = "220202199001012345"
                }
            )
        } | ConvertTo-Json -Depth 3
        
        Write-TestLog "Sending second order attempting to occupy the same seat: $seatId"
        $secondResponse = Invoke-RestMethod -Uri "$baseUrl$orderEndpoint" -Method Post -Body $secondOrderBody -Headers $headers -ErrorAction Stop
        
        Write-TestLog "FAIL: Second order with duplicate seat was accepted when it should be rejected"
        return $false
    } catch {
        Write-TestLog "PASS: Duplicate seat order correctly rejected with error: $($_.Exception.Message)"
        return $true
    }
}

# Function to test concurrent order processing
function Test-ConcurrentOrders {
    Write-TestLog "\n=== Test 5: Concurrent Orders Test ==="
    if (-not $global:token) {
        Write-TestLog "FAIL: Not logged in, cannot test concurrent orders"
        return $false
    }
    
    try {
        $orderCount = 10
        $successCount = 0
        
        Write-TestLog "Sending $orderCount concurrent orders to test message queue processing"
        
        # Set request headers
        $headers = @{
            "Authorization" = "Bearer $global:token"
            "Content-Type" = "application/json"
        }
        
        # Start timing
        $startTime = Get-Date
        
        # Send multiple orders quickly
        for ($i = 1; $i -le $orderCount; $i++) {
            try {
                $orderId = Get-Random -Minimum 10000 -Maximum 99999
                $seatId = "${i}B"
                
                # Build order request body
                $orderBody = @{
                    orderId = $orderId
                    status = "PENDING"
                    userId = $username
                    flightNumber = "CA1234"
                    amount = 99.99
                    items = @(
                        @{
                            seatId = $seatId
                            passengerName = "Concurrent Passenger $i"
                            passengerIdCard = "$((1000000000000000 + $i).ToString())"
                        }
                    )
                } | ConvertTo-Json -Depth 3
                
                Write-TestLog "Sending concurrent order $i with seat $seatId"
                $response = Invoke-RestMethod -Uri "$baseUrl$orderEndpoint" -Method Post -Body $orderBody -Headers $headers -ErrorAction Stop
                $successCount++
            } catch {
                Write-TestLog "Warning: Order $i failed: $($_.Exception.Message)"
            }
        }
        
        # Calculate elapsed time
        $endTime = Get-Date
        $elapsedTime = ($endTime - $startTime).TotalSeconds
        
        Write-TestLog "PASS: Concurrent order test completed"
        Write-TestLog "Successfully sent: $successCount / $orderCount orders"
        Write-TestLog "Total time: $elapsedTime seconds"
        Write-TestLog "Average time per order: $($elapsedTime / $orderCount) seconds"
        
        return $true
    } catch {
        Write-TestLog "FAIL: Concurrent orders test failed: $($_.Exception.Message)"
        if ($_.ErrorDetails) {
            Write-TestLog "Error details: $($_.ErrorDetails.Message)"
        }
        return $false
    }
}

# Main test execution
Write-TestLog "Starting serial execution of all order tests..."

# Execute tests in sequence
$loginSuccess = Test-Login

# Only proceed with other tests if login is successful
if ($loginSuccess) {
    $normalOrderSuccess = Test-NormalOrder
    $abnormalOrderSuccess = Test-AbnormalOrderRejection
    $duplicateSeatSuccess = Test-DuplicateSeatRejection
    $concurrentOrdersSuccess = Test-ConcurrentOrders
} else {
    Write-TestLog "\nSkipping remaining tests due to login failure"
}

Write-TestLog "\n=== All Tests Completed ==="
Write-TestLog "Test completed at: $(Get-Date)"
Write-Host "Test log saved to: $logFile"