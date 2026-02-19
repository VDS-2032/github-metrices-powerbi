# ============================================================
# GitHub Metrics Data Refresh - PowerShell Script
# For use with Windows Task Scheduler
# ============================================================

param(
    [string]$ConfigPath = ".\scripts\config.json",
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

# Get script directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " GitHub Repository Metrics - Data Refresh" -ForegroundColor Cyan
Write-Host " Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Check Python installation
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✓ Python detected: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Python is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Navigate to scripts directory
Set-Location "$ScriptDir\scripts"

# Install/update dependencies
Write-Host ""
Write-Host "Installing dependencies..." -ForegroundColor Yellow
pip install -r requirements.txt --quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ ERROR: Failed to install dependencies" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Dependencies installed" -ForegroundColor Green

# Run the data extractor
Write-Host ""
Write-Host "Extracting GitHub metrics..." -ForegroundColor Yellow
python github_data_extractor.py
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ ERROR: Data extraction failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host " Data Refresh Completed Successfully!" -ForegroundColor Green
Write-Host " Finished: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

# Return to original directory
Set-Location $ScriptDir

exit 0

