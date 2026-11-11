# ===================================================================
# BanknoteGuard DL4J - PowerShell Launch Script
# Resolves Maven and Directory path automatically
# ===================================================================

$ErrorActionPreference = "Stop"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  BanknoteGuard DL4J - Automatic Launcher (PowerShell)          " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Resolve working directory properly (whether run from root or inside banknote-authentication)
$currentDir = Get-Location
if (Test-Path "$currentDir\banknote-authentication\pom.xml") {
    Set-Location -Path "$currentDir\banknote-authentication"
} elseif (Test-Path "$PSScriptRoot\banknote-authentication\pom.xml") {
    Set-Location -Path "$PSScriptRoot\banknote-authentication"
}

Write-Host "Project directory: $(Get-Location)" -ForegroundColor Gray

# 2. Verify Java installation
try {
    $javaVer = java -version 2>&1 | Select-Object -First 1
    Write-Host "[OK] Java is installed: $javaVer" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java is not installed or not in PATH. Please install JDK 17 or JDK 21." -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit 1
}

# 3. Locate or download Maven automatically
$mvnCommand = Get-Command mvn -ErrorAction SilentlyContinue
$mvnExec = ""

if ($mvnCommand) {
    $mvnExec = "mvn"
    Write-Host "[OK] System Maven detected." -ForegroundColor Green
} else {
    $wrapperMavenDir = "$env:USERPROFILE\.m2\wrapper\apache-maven-3.9.6"
    $wrapperMvnBin = "$wrapperMavenDir\bin\mvn.cmd"

    if (-not (Test-Path $wrapperMvnBin)) {
        Write-Host "[INFO] Maven is not installed in Windows PATH." -ForegroundColor Yellow
        Write-Host "[INFO] Automatically downloading portable Apache Maven 3.9.6 to $wrapperMavenDir..." -ForegroundColor Cyan

        New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\.m2\wrapper" | Out-Null
        $zipPath = "$env:USERPROFILE\.m2\wrapper\apache-maven-3.9.6-bin.zip"

        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile $zipPath
        
        Write-Host "[INFO] Extracting Maven..." -ForegroundColor Cyan
        Expand-Archive -Path $zipPath -DestinationPath "$env:USERPROFILE\.m2\wrapper" -Force
        Remove-Item $zipPath -Force
    }

    $mvnExec = $wrapperMvnBin
    Write-Host "[OK] Portable Maven is ready: $mvnExec" -ForegroundColor Green
}

# 4. Start the Application
Write-Host ""
Write-Host "Starting Banknote Authentication Spring Boot Application (port 8080)..." -ForegroundColor Green
Write-Host "Web Interface: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Admin Login:   admin@banknote.com / Admin@123" -ForegroundColor Gray
Write-Host "User Login:    user@banknote.com  / User@123" -ForegroundColor Gray
Write-Host ""

& $mvnExec spring-boot:run -Dspring-boot.run.profiles=h2

