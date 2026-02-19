@echo off
REM ============================================================
REM GitHub Metrics Data Refresh Script
REM Run this script to extract latest data from GitHub
REM ============================================================

echo.
echo ============================================================
echo GitHub Repository Metrics - Data Refresh
echo ============================================================
echo.

cd /d "%~dp0scripts"

echo Checking Python installation...
python --version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Python is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo Installing/Updating dependencies...
pip install -r requirements.txt --quiet

echo.
echo Extracting GitHub metrics...
python github_data_extractor.py

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Data extraction failed!
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Data refresh completed successfully!
echo CSV files are available in the data folder.
echo ============================================================
echo.

pause

