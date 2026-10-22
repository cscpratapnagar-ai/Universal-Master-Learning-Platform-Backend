[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd("/")
$script:passed = 0
$script:failed = 0

function Invoke-SmokeRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][int]$ExpectedStatus,
        [scriptblock]$Validate
    )

    $tempFile = [System.IO.Path]::GetTempFileName()
    try {
        $statusText = & curl.exe --silent --show-error --output $tempFile --write-out "%{http_code}" "$BaseUrl$Path"
        if ($LASTEXITCODE -ne 0) {
            throw "curl.exe failed with exit code $LASTEXITCODE"
        }

        $actualStatus = [int]$statusText
        $body = Get-Content -LiteralPath $tempFile -Raw

        if ($actualStatus -ne $ExpectedStatus) {
            throw "Expected HTTP $ExpectedStatus but received HTTP $actualStatus. Body: $body"
        }

        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($body)) {
            try { $json = $body | ConvertFrom-Json } catch { }
        }

        if ($Validate) {
            & $Validate $json $body
        }

        Write-Host "[PASS] $Name (HTTP $actualStatus)" -ForegroundColor Green
        $script:passed++
    }
    catch {
        Write-Host "[FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
    finally {
        Remove-Item -LiteralPath $tempFile -Force -ErrorAction SilentlyContinue
    }
}

if (-not (Get-Command curl.exe -ErrorAction SilentlyContinue)) {
    Write-Error "curl.exe was not found. Run this script on Windows with curl.exe installed."
    exit 2
}

Write-Host ""
Write-Host "Universal Master Learning Platform - Local Smoke Test" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl"
Write-Host ""

Invoke-SmokeRequest -Name "Actuator health and database connection" -Path "/actuator/health" -ExpectedStatus 200 -Validate {
    param($json, $body)
    if ($json.status -ne "UP") { throw "Health status is not UP." }
    if ($json.components.db.status -ne "UP") { throw "Database health is not UP." }
}

Invoke-SmokeRequest -Name "Public platform status" -Path "/api/v1/public/status" -ExpectedStatus 200 -Validate {
    param($json, $body)
    if ($json.data.status -ne "ONLINE") { throw "Platform status is not ONLINE." }
}

Invoke-SmokeRequest -Name "OpenAPI document" -Path "/v3/api-docs" -ExpectedStatus 200 -Validate {
    param($json, $body)
    if (-not $json.paths) { throw "OpenAPI document does not contain paths." }
    if ($json.paths.PSObject.Properties.Name -notcontains "/api/v1/auth/login") {
        throw "Login endpoint is missing from OpenAPI paths."
    }
}

Invoke-SmokeRequest -Name "Protected profile rejects anonymous request" -Path "/api/v1/auth/me" -ExpectedStatus 401 -Validate {
    param($json, $body)
    if ($json.code -ne "UNAUTHORIZED") { throw "Expected an UNAUTHORIZED error response." }
}

Write-Host ""
Write-Host "Summary: $script:passed passed, $script:failed failed." -ForegroundColor Cyan
if ($script:failed -gt 0) { exit 1 }
exit 0
