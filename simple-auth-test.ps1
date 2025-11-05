# Simple Authentication Test Script
Write-Output "=== Simple Authentication Test ==="

# Set character encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

# Test environment configuration
$baseUrl = "http://localhost:8080/api"
$loginEndpoint = "/auth/login"
$meEndpoint = "/auth/me"

# User credentials
$username = "user"
$password = "user123"

# Step 1: Login with user
Write-Output "\nStep 1: Logging in with user: $username"
try {
    # Build login request body
    $loginBody = @{
        username = $username
        password = $password
    } | ConvertTo-Json
    
    Write-Output "Login request body: $loginBody"
    
    # Send login request
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl$loginEndpoint" -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    
    # Extract token and user info from ApiResponse wrapper
    Write-Output "Login response structure: $(ConvertTo-Json $loginResponse -Depth 3)"
    
    $token = $loginResponse.data.token
    $userRole = $loginResponse.data.user.role
    
    Write-Output "✅ Login Successful!"
    Write-Output "Token: $token"
    Write-Output "User: $username, Role: $userRole"
    
    # Step 2: Verify token validity
    Write-Output "\nStep 2: Verifying token validity - Getting current user info"
    try {
        $meHeaders = @{
            "Authorization" = "Bearer $token"
        }
        
        Write-Output "Sending request with Authorization header: Bearer $token"
        $meResponse = Invoke-RestMethod -Uri "$baseUrl$meEndpoint" -Method Get -Headers $meHeaders -ErrorAction Stop
        
        Write-Output "✅ Token is valid!"
        Write-Output "Current user: $($meResponse.username), Role: $($meResponse.role)"
        
    } catch {
        Write-Output "❌ Token verification failed: $($_.Exception.Message)"
        if ($_.ErrorDetails) {
            Write-Output "Error details: $($_.ErrorDetails.Message)"
        }
        Write-Output "Error object:"
        $_ | Format-List -Force | Out-String | Write-Output
    }
    
} catch {
    Write-Output "❌ Login failed: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Output "Error details: $($_.ErrorDetails.Message)"
    }
    Write-Output "Error object:"
    $_ | Format-List -Force | Out-String | Write-Output
}

Write-Output "\n=== Test Completed ==="