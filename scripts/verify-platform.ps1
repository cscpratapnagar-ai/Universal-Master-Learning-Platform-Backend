[CmdletBinding()]
param(
    [switch]$SkipBackendTests,
    [switch]$SkipUiTests,
    [switch]$SkipSmokeTest
)

$ErrorActionPreference = 'Stop'
$backendRoot = Split-Path -Parent $PSScriptRoot
$workspaceRoot = Split-Path -Parent $backendRoot
$uiRoot = Join-Path $workspaceRoot 'Universal-Master-Learning-Platform-UI'
$script:passed = 0
$script:failed = 0
$script:skipped = 0

function Invoke-Check {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][scriptblock]$Command
    )

    Write-Host "`n=== $Name ===" -ForegroundColor Cyan
    & $Command
    if ($LASTEXITCODE -ne 0) {
        $script:failed++
        throw "$Name failed with exit code $LASTEXITCODE."
    }
    $script:passed++
    Write-Host "[PASS] $Name" -ForegroundColor Green
}

Write-Host 'Universal Master Learning Platform - Full Verification' -ForegroundColor Cyan
Write-Host "Backend: $backendRoot"
Write-Host "UI:      $uiRoot"

if (-not (Test-Path (Join-Path $backendRoot 'pom.xml'))) {
    throw "Backend pom.xml not found. Run this script from the backend repository."
}
if (-not (Test-Path (Join-Path $uiRoot 'package.json'))) {
    throw "UI repository not found at '$uiRoot'. Keep the UI and backend repositories as sibling folders."
}

if (-not $SkipBackendTests) {
    Push-Location $backendRoot
    try {
        Invoke-Check -Name 'Backend Maven test suite' -Command {
            mvn --batch-mode --no-transfer-progress test
        }
    }
    finally { Pop-Location }
}
else {
    $script:skipped++
    Write-Host '[SKIP] Backend Maven tests' -ForegroundColor Yellow
}

if (-not $SkipSmokeTest) {
    try {
        $health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 3
        if ($health.status -ne 'UP' -or $health.components.db.status -ne 'UP') {
            throw 'Backend or database health is not UP.'
        }

        Push-Location $backendRoot
        try {
            Invoke-Check -Name 'Live API and database smoke tests' -Command {
                & (Join-Path $backendRoot 'scripts/smoke-test-local.ps1')
            }
        }
        finally { Pop-Location }
    }
    catch {
        $script:skipped++
        Write-Host "[SKIP] Live smoke tests: backend is not healthy/reachable at localhost:8080. Start the backend and rerun to include live API checks. Details: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
else {
    $script:skipped++
    Write-Host '[SKIP] Live API smoke tests (requested)' -ForegroundColor Yellow
}

if (-not $SkipUiTests) {
    Push-Location $uiRoot
    try {
        if (-not (Test-Path (Join-Path $uiRoot 'node_modules'))) {
            Invoke-Check -Name 'Install UI dependencies (npm ci)' -Command {
                npm ci
            }
        }

        Invoke-Check -Name 'Angular production build' -Command {
            npm run build
        }

        Invoke-Check -Name 'Angular unit tests (ChromeHeadless)' -Command {
            npm test -- --watch=false --browsers=ChromeHeadless
        }
    }
    finally { Pop-Location }
}
else {
    $script:skipped++
    Write-Host '[SKIP] UI build and unit tests' -ForegroundColor Yellow
}

Write-Host "`n=== Verification Summary ===" -ForegroundColor Cyan
Write-Host "Passed:  $script:passed" -ForegroundColor Green
Write-Host "Failed:  $script:failed" -ForegroundColor Red
Write-Host "Skipped: $script:skipped" -ForegroundColor Yellow
if ($script:failed -gt 0) { exit 1 }
Write-Host 'Verification completed. Review any SKIP messages above.' -ForegroundColor Cyan
