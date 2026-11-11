Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host "  Pushing Banknote Authentication System to GitHub                 " -ForegroundColor Cyan
Write-Host "  Repository: https://github.com/Ganesh-0411/banknote-authentication-system.git" -ForegroundColor Gray
Write-Host "===================================================================" -ForegroundColor Cyan

Set-Location -Path $PSScriptRoot

# 1. Search Git binary
$gitPath = (Get-Command git -ErrorAction SilentlyContinue)?.Source
if (-not $gitPath) {
    $searchPaths = @(
        "C:\Program Files\Git\cmd\git.exe",
        "C:\Program Files\Git\bin\git.exe",
        "C:\Program Files (x86)\Git\cmd\git.exe",
        "$env:LOCALAPPDATA\Programs\Git\cmd\git.exe",
        "$env:USERPROFILE\AppData\Local\Programs\Git\cmd\git.exe"
    )
    foreach ($p in $searchPaths) {
        if (Test-Path $p) {
            $gitPath = $p
            $env:Path = [System.IO.Path]::GetDirectoryName($p) + ";" + $env:Path
            break
        }
    }
}

if (-not $gitPath) {
    Write-Host "[INFO] Git not found. Attempting install via winget..." -ForegroundColor Yellow
    winget install --id Git.Git -e --source winget --accept-source-agreements --accept-package-agreements
    if (Test-Path "C:\Program Files\Git\cmd\git.exe") {
        $gitPath = "C:\Program Files\Git\cmd\git.exe"
        $env:Path = "C:\Program Files\Git\cmd;" + $env:Path
    } else {
        Write-Host "[ACTION REQUIRED] Please install Git from https://git-scm.com/download/win and rerun." -ForegroundColor Red
        Read-Host "Press Enter to exit..."
        exit 1
    }
}

Write-Host "[OK] Using Git: $gitPath" -ForegroundColor Green

if (-not (Test-Path ".git")) {
    & $gitPath init
}

& $gitPath branch -M main
& $gitPath remote remove origin 2>$null
& $gitPath remote add origin https://github.com/Ganesh-0411/banknote-authentication-system.git

& $gitPath add .

$userName = (& $gitPath config user.name)
if (-not $userName) {
    & $gitPath config user.name "Ganesh-0411"
    & $gitPath config user.email "ganesh@users.noreply.github.com"
}

& $gitPath commit -m "Complete Deep Learning Banknote Authentication System using DL4J CNN Autoencoder, OpenCV Java, and Spring Boot"
& $gitPath push -u origin main --force

Write-Host "`n[SUCCESS] Code pushed to: https://github.com/Ganesh-0411/banknote-authentication-system" -ForegroundColor Green
