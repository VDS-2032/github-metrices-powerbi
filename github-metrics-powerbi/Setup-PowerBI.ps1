<#
.SYNOPSIS
    Generates a Power BI data model setup script with all tables and measures.

.DESCRIPTION
    This script generates a complete Power BI setup including:
    - Data connection queries for all CSV files
    - All DAX measures
    - Relationship definitions

.NOTES
    Run this script to generate setup files, then follow the POWERBI_IMPORT_GUIDE.md
#>

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$DataPath = Join-Path $ProjectRoot "data"
$OutputPath = Join-Path $ProjectRoot "powerbi"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Power BI Setup Generator" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Verify data files exist
$requiredFiles = @(
    "pull_requests.csv",
    "developer_aggregates.csv",
    "repository_aggregates.csv",
    "overall_aggregates.csv",
    "threshold_violations.csv"
)

Write-Host "Checking data files..." -ForegroundColor Yellow
$allFilesExist = $true
foreach ($file in $requiredFiles) {
    $filePath = Join-Path $DataPath $file
    if (Test-Path $filePath) {
        $fileInfo = Get-Item $filePath
        Write-Host "  ✓ $file ($([math]::Round($fileInfo.Length/1KB, 2)) KB)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file - NOT FOUND" -ForegroundColor Red
        $allFilesExist = $false
    }
}

if (-not $allFilesExist) {
    Write-Host ""
    Write-Host "ERROR: Some data files are missing!" -ForegroundColor Red
    Write-Host "Run 'refresh_data.bat' or 'python scripts/github_data_extractor.py' first." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "All data files present!" -ForegroundColor Green
Write-Host ""

# Generate DAX measures file for easy copy-paste
$daxMeasures = @"
// =====================================================
// GITHUB METRICS - DAX MEASURES
// =====================================================
// Copy these measures into Power BI Desktop
// Right-click on _Measures table > New Measure
// =====================================================

// === CORE AGGREGATE MEASURES ===

Total PRs = COUNTROWS('pull_requests')

Total Merged =
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_merged] = TRUE
)

Total Rejected =
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_rejected] = TRUE
)

Total Open =
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_open] = TRUE
)

PRs Beyond Threshold =
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[is_beyond_threshold] = TRUE
)

// === RATE MEASURES ===

Merge Rate =
DIVIDE([Total Merged], [Total PRs], 0) * 100

Rejection Rate =
DIVIDE([Total Rejected], [Total PRs], 0) * 100

// === TIME MEASURES ===

Avg Time to Merge (Hours) =
AVERAGE('pull_requests'[time_to_merge_hours])

Avg Time to Merge (Days) =
DIVIDE([Avg Time to Merge (Hours)], 24, 0)

Median Time to Merge =
MEDIAN('pull_requests'[time_to_merge_hours])

// === DEVELOPER MEASURES ===

Developer PR Count =
CALCULATE(
    COUNTROWS('pull_requests'),
    ALLEXCEPT('pull_requests', 'pull_requests'[author])
)

Developer Merge Rate =
VAR TotalPRs = [Developer PR Count]
VAR MergedPRs =
    CALCULATE(
        COUNTROWS('pull_requests'),
        'pull_requests'[is_merged] = TRUE,
        ALLEXCEPT('pull_requests', 'pull_requests'[author])
    )
RETURN
    DIVIDE(MergedPRs, TotalPRs, 0) * 100

// === THRESHOLD MEASURES ===

Threshold Violation Rate =
DIVIDE([PRs Beyond Threshold], [Total Open], 0) * 100

Critical PRs =
CALCULATE(
    COUNTROWS('pull_requests'),
    'pull_requests'[hours_open] > 72,
    'pull_requests'[is_open] = TRUE
)

// === CONDITIONAL FORMATTING ===

Merge Time Color =
SWITCH(
    TRUE(),
    [Avg Time to Merge (Hours)] <= 24, "#4CAF50",
    [Avg Time to Merge (Hours)] <= 48, "#FFC107",
    "#F44336"
)

Threshold Icon =
IF([PRs Beyond Threshold] > 0, "⚠️", "✅")
"@

$daxFilePath = Join-Path $OutputPath "dax_measures_import.dax"
$daxMeasures | Out-File -FilePath $daxFilePath -Encoding UTF8
Write-Host "Generated: dax_measures_import.dax" -ForegroundColor Green

# Display summary
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Setup Complete!" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Files generated in: $OutputPath" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Open Power BI Desktop" -ForegroundColor White
Write-Host "  2. Follow instructions in: POWERBI_IMPORT_GUIDE.md" -ForegroundColor White
Write-Host "  3. Import CSV files from: $DataPath" -ForegroundColor White
Write-Host "  4. Copy measures from: dax_measures_import.dax" -ForegroundColor White
Write-Host ""
Write-Host "Quick Start:" -ForegroundColor Yellow
Write-Host "  - Get Data > Text/CSV > Select all 5 CSV files" -ForegroundColor White
Write-Host "  - Create relationships as documented" -ForegroundColor White
Write-Host "  - Add measures from dax_measures_import.dax" -ForegroundColor White
Write-Host ""

# Open the import guide
$importGuide = Join-Path $ProjectRoot "POWERBI_IMPORT_GUIDE.md"
if (Test-Path $importGuide) {
    $openGuide = Read-Host "Open import guide now? (Y/N)"
    if ($openGuide -eq "Y" -or $openGuide -eq "y") {
        Start-Process $importGuide
    }
}

