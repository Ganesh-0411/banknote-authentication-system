# ===================================================================
# BanknoteGuard DL4J - PowerShell Build & Package Script
# ===================================================================

$ErrorActionPreference = "Stop"

$currentDir = Get-Location
if (Test-Path "$currentDir\banknote-authentication\pom.xml") {
    Set-Location -Path "$currentDir\banknote-authentication"
} elseif (Test-Path "$PSScriptRoot\pom.xml") {
    Set-Location -Path "$PSScriptRoot"
}

$mvnCommand = Get-Command mvn -ErrorAction SilentlyContinue
$mvnExec = ""

if ($mvnCommand) {
    $mvnExec = "mvn"
} else {
    $wrapperMvnBin = "$env:USERPROFILE\.m2\wrapper\apache-maven-3.9.6\bin\mvn.cmd"
    if (-not (Test-Path $wrapperMvnBin)) {
        & ".\run.ps1"
        exit 0
    }
    $mvnExec = $wrapperMvnBin
}

Write-Host "Compiling and packaging Banknote Authentication System..." -ForegroundColor Green
& $mvnExec clean package -DskipTests
Write-Host "Build complete! Target JAR created in target\" -ForegroundColor Cyan

