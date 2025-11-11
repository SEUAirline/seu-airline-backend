# -------------------------
# Integrated Test Script:
# Login + Simulate Redis browsing history + Call /personalized-recommendations
# -------------------------
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

# --- Configuration ---
$hostUrl = "http://localhost:8080"
$loginUrl = "$hostUrl/api/auth/login"
$recommendUrl = "$hostUrl/api/flight/personalized-recommendations?limit=5"
$redisCli = "redis-cli"   # assumes redis-cli in PATH
$userId = 5               # user ID to simulate
$flightIds = @(7, 8, 9, 10, 11)  # simulated viewed flights

$username = "user"
$password = "user123"

Write-Output "=== Flight Recommendation Integrated Test ==="
Write-Output ""

# -------------------------
# Step 1: Login
# -------------------------
Write-Output "-- Step 1: Logging in --"

$loginBody = @{ username = $username; password = $password } | ConvertTo-Json -Compress
Write-Output "Login URL: $loginUrl"
Write-Output "Login Body: $loginBody"

try {
    $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Output "Login response (raw):"
    $loginResponse | ConvertTo-Json -Depth 5 | Write-Output

    if ($null -eq $loginResponse.data) {
        Write-Output "❌ Login returned no data. Message: $($loginResponse.message)"
        exit 1
    }
    $token = $loginResponse.data.token
    if ([string]::IsNullOrEmpty($token)) {
        Write-Output "❌ Token not found in login response. Full data:"
        $loginResponse.data | ConvertTo-Json -Depth 5 | Write-Output
        exit 1
    }
    Write-Output "✅ Login successful! Token obtained."
    Write-Output ""
} catch {
    Write-Output "❌ Login failed: $($_.Exception.Message)"
    exit 1
}

# -------------------------
# Step 2: Simulate browsing history in Redis
# -------------------------
Write-Output "-- Step 2: Simulating user browsing history in Redis --"

foreach ($fid in $flightIds) {
    $cmd = "LPUSH user:history:$userId `"$fid`""
    Write-Output "Executing: $cmd"
    & $redisCli LPUSH "user:history:$userId" "$fid" | Out-Null
}

Write-Output "`n--- Redis verification ---"
& $redisCli LRANGE "user:history:$userId" 0 -1

Write-Output "✅ Browsing history successfully written to Redis."
Write-Output ""

# -------------------------
# Step 3: Fetch personalized recommendations
# -------------------------
Write-Output "-- Step 3: Fetching personalized recommendations --"
Write-Output "URL: $recommendUrl"

$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}

try {
    $recommendResponse = Invoke-RestMethod -Uri $recommendUrl -Method Get -Headers $headers -ErrorAction Stop
    Write-Output "✅ Recommendation API call successful!"
    Write-Output "Response (formatted):"
    $recommendResponse | ConvertTo-Json -Depth 5 | Write-Output
} catch {
    Write-Output "❌ Error while calling recommendation API: $($_.Exception.Message)"
    if ($_.Exception.Response -ne $null) {
        try {
            $bodyStream = $_.Exception.Response.GetResponseStream()
            $sr = New-Object System.IO.StreamReader($bodyStream)
            $respText = $sr.ReadToEnd()
            Write-Output "Server response body:`n$respText"
        } catch {}
    }
    exit 1
}

Write-Output "`n=== Test completed ==="
