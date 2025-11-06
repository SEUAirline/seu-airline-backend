# Test RabbitMQ Interface Access - Enhanced Version
Write-Output "=== RabbitMQ Interface Testing Tool ==="

# Set character encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

# Test environment configuration
$baseUrl = "http://localhost:8080/api"
$loginEndpoint = "/auth/login"
$orderEndpoint = "/rabbitmq/order"
$logEndpoint = "/rabbitmq/log"
$notificationEndpoint = "/rabbitmq/notification"
$meEndpoint = "/auth/me"

# User credentials
$username = "user"
$password = "user123"

Write-Output "\n=== Starting Serial Tests for All Message Types ==="

# Step 1: Login with passenger user
Write-Output "\nStep 1: Logging in with passenger user"
try {
    # Build login request body
    $loginBody = @{
        username = $username
        password = $password
    } | ConvertTo-Json
    
    # Send login request
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl$loginEndpoint" -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    
    # Extract token and user info from ApiResponse wrapper
    $token = $loginResponse.data.token
    $userRole = $loginResponse.data.user.role
    
    Write-Output "✅ Login Successful!"
    Write-Output "Token: $token"
    Write-Output "User: $username, Role: $userRole"
    Write-Output "Message: $($loginResponse.message)"
    
    # Function to test order message
    function Test-OrderMessage {
        param(
            [string]$token
        )
        
        Write-Output "\n=== Testing Order Message ==="
        try {
            # Build order message request body matching OrderMessage class structure
            $orderBody = @{
                orderId = 1001
                status = "PENDING"
                userId = "user"
                flightNumber = "CA1234"
                amount = 99.99
            } | ConvertTo-Json -Depth 3
            
            Write-Output "Sending Order Data:"
            Write-Output $orderBody
            
            # Set request headers
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json"
            }
            
            # Send order message request
            $orderResponse = Invoke-RestMethod -Uri "$baseUrl$orderEndpoint" -Method Post -Body $orderBody -Headers $headers -ErrorAction Stop
            
            Write-Output "Order message sent successfully!"
            Write-Output "Response: $orderResponse"
            return $true
        } catch {
            Write-Output "Order message test failed: $($_.Exception.Message)"
            if ($_.ErrorDetails) {
                Write-Output "Error details: $($_.ErrorDetails.Message)"
            }
            return $false
        }
    }
    
    # Function to test log message
    function Test-LogMessage {
        param(
            [string]$token
        )
        
        Write-Output "\n=== Testing Log Message ==="
        try {
            # Build log message request body matching LogMessage class structure
            $logBody = @{
                level = "INFO"
                className = "TestClass"
                methodName = "testMethod"
                content = "This is a test log message from PowerShell script"
                ip = "127.0.0.1"
                userId = "user"
            } | ConvertTo-Json -Depth 3
            
            Write-Output "Sending Log Data:"
            Write-Output $logBody
            
            # Set request headers
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json"
            }
            
            # Send log message request
            $logResponse = Invoke-RestMethod -Uri "$baseUrl$logEndpoint" -Method Post -Body $logBody -Headers $headers -ErrorAction Stop
            
            Write-Output "Log message sent successfully!"
            Write-Output "Response: $logResponse"
            return $true
        } catch {
            Write-Output "Log message test failed: $($_.Exception.Message)"
            if ($_.ErrorDetails) {
                Write-Output "Error details: $($_.ErrorDetails.Message)"
            }
            return $false
        }
    }
    
    # Function to test notification message
    function Test-NotificationMessage {
        param(
            [string]$token
        )
        
        Write-Output "\n=== Testing Notification Message ==="
        try {
            # Build notification message request body matching NotificationMessage class structure
            $notificationBody = @{
                recipient = "user@example.com"
                title = "Test Notification"
                content = "This is a test notification message from PowerShell script"
                notificationType = "EMAIL"
                sender = "system"
            } | ConvertTo-Json -Depth 3
            
            Write-Output "Sending Notification Data:"
            Write-Output $notificationBody
            
            # Set request headers
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json"
            }
            
            # Send notification message request
            $notificationResponse = Invoke-RestMethod -Uri "$baseUrl$notificationEndpoint" -Method Post -Body $notificationBody -Headers $headers -ErrorAction Stop
            
            Write-Output "Notification message sent successfully!"
            Write-Output "Response: $notificationResponse"
            return $true
        } catch {
            Write-Output "Notification message test failed: $($_.Exception.Message)"
            if ($_.ErrorDetails) {
                Write-Output "Error details: $($_.ErrorDetails.Message)"
            }
            return $false
        }
    }
    
    # Step 2: Executing serial tests for all message types
    Write-Output "\nStep 2: Running serial tests for all message types"
    
    # Direct test execution with simple result tracking
    $successCount = 0
    $totalCount = 3
    
    Write-Output "Starting order message test..."
    $orderSuccess = Test-OrderMessage -token $token
    if ($orderSuccess) { $successCount++ }
    
    Write-Output "\nStarting log message test..."
    $logSuccess = Test-LogMessage -token $token
    if ($logSuccess) { $successCount++ }
    
    Write-Output "\nStarting notification message test..."
    $notificationSuccess = Test-NotificationMessage -token $token
    if ($notificationSuccess) { $successCount++ }
    
    # Step 3: Verify token validity and show final results
    Write-Output "\nStep 3: Verifying token validity and finalizing test"
    try {
        $meHeaders = @{
            "Authorization" = "Bearer $token"
        }
        $meResponse = Invoke-RestMethod -Uri "$baseUrl$meEndpoint" -Method Get -Headers $meHeaders -ErrorAction Stop
        
        Write-Output "Token is still valid!"
        Write-Output "Current user: $($meResponse.data.username), Role: $($meResponse.data.role)"
        
        # Display test summary
        Write-Output "\n=== Test Summary ==="
        Write-Output "Total tests: $totalCount"
        Write-Output "Successful tests: $successCount"
        Write-Output "Failed tests: $($totalCount - $successCount)"
        
        if ($successCount -eq $totalCount) {
            Write-Output "All tests completed successfully!"
        } else {
            Write-Output "⚠️ Some tests failed. Please check the output above for details."
        }
        
    } catch {
        Write-Output "Token verification failed: $($_.Exception.Message)"
        
        # Even if token verification fails, show test results
        Write-Output "\n=== Test Summary ==="
        Write-Output "Total tests: $totalCount"
        Write-Output "Successful tests: $successCount"
        Write-Output "Failed tests: $($totalCount - $successCount)"
    }
    
} catch {
    Write-Output "Login failed: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Output "Error details: $($_.ErrorDetails.Message)"
    }
}

Write-Output "\n=== Test Completed ==="