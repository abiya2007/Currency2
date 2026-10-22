# Smart Portable Java Launcher Script for PowerShell
param (
    [switch]$BuildOnly = $false
)

$ErrorActionPreference = "Stop"

Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host "   Global Currency Converter - Java Web Application Server     " -ForegroundColor Cyan
Write-Host "===============================================================" -ForegroundColor Cyan

# 1. Function to locate javac.exe and java.exe
function Find-JavaRuntime {
    # Check PATH first
    $javacInPath = Get-Command javac -ErrorAction SilentlyContinue
    $javaInPath = Get-Command java -ErrorAction SilentlyContinue
    if ($javacInPath -and $javaInPath) {
        return @{ Javac = $javacInPath.Source; Java = $javaInPath.Source }
    }

    # Check JAVA_HOME
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
        return @{ Javac = "$env:JAVA_HOME\bin\javac.exe"; Java = "$env:JAVA_HOME\bin\java.exe" }
    }

    # Search common Windows installation paths
    $searchPaths = @(
        "$env:USERPROFILE\.antigravity\extensions",
        "$env:USERPROFILE\.vscode\extensions",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Amazon Corretto",
        "C:\Program Files\Microsoft",
        "C:\Program Files (x86)\Java"
    )

    foreach ($dir in $searchPaths) {
        if (Test-Path $dir) {
            $foundJavac = Get-ChildItem -Path $dir -Filter "javac.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($foundJavac) {
                $binDir = Split-Path $foundJavac.FullName
                $foundJava = Join-Path $binDir "java.exe"
                if (Test-Path $foundJava) {
                    return @{ Javac = $foundJavac.FullName; Java = $foundJava }
                }
            }
        }
    }

    return $null
}

$rt = Find-JavaRuntime

if (-not $rt) {
    Write-Host ""
    Write-Host "[ERROR] Java Development Kit (JDK 21 or higher) was not found on this computer!" -ForegroundColor Red
    Write-Host "Please install Java JDK from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Yellow
    Write-Host "Or ensure Extension Pack for Java is installed in VS Code." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host "Using Java Compiler : $($rt.Javac)" -ForegroundColor Gray
Write-Host "Using Java Runtime  : $($rt.Java)" -ForegroundColor Gray

# Ensure output directory bin exists
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Collect source files
$sources = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

Write-Host ""
Write-Host "[1/2] Compiling Java code to bin/..." -ForegroundColor Yellow
$compileProcess = Start-Process -FilePath $rt.Javac -ArgumentList "-d bin -encoding UTF-8 `"$($sources -join '`" `"')`"" -NoNewWindow -Wait -PassThru

if ($compileProcess.ExitCode -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Compilation failed with exit code $($compileProcess.ExitCode)!" -ForegroundColor Red
    exit 1
}

Write-Host "Compilation successful." -ForegroundColor Green

if ($BuildOnly) {
    exit 0
}

# Stop any existing process running on port 8080
$existingPortPids = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($existingPortPids) {
    foreach ($procId in $existingPortPids) {
        Write-Host "Stopping existing process on port 8080 (PID: $procId)..." -ForegroundColor Yellow
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 1
}

Write-Host ""
Write-Host "[2/2] Launching Currency Converter Java Server on http://localhost:8080..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the server." -ForegroundColor Gray
Write-Host ""

& $rt.Java -cp bin com.currencyconverter.CurrencyConverterServer
