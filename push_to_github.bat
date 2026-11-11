@echo off
setlocal enabledelayedexpansion

echo ===================================================================
echo   Updating Banknote Authentication System on GitHub
echo   Target: https://github.com/Ganesh-0411/banknote-authentication-system.git
echo ===================================================================
echo.

cd /d "%~dp0"

:: 1. Search for git.exe in PATH and all common Windows installation paths
set "GIT_BIN=git"
where git >nul 2>nul
if %ERRORLEVEL% equ 0 goto git_found

for %%P in (
    "C:\Program Files\Git\cmd\git.exe"
    "C:\Program Files\Git\bin\git.exe"
    "C:\Program Files (x86)\Git\cmd\git.exe"
    "C:\Program Files (x86)\Git\bin\git.exe"
    "%LOCALAPPDATA%\Programs\Git\cmd\git.exe"
    "%LOCALAPPDATA%\Programs\Git\bin\git.exe"
    "%USERPROFILE%\AppData\Local\Programs\Git\cmd\git.exe"
    "%USERPROFILE%\scoop\shims\git.exe"
    "C:\ProgramData\chocolatey\bin\git.exe"
) do (
    if exist "%%~P" (
        set "GIT_BIN=%%~P"
        for %%D in ("%%~dpP") do set "PATH=%%~D;!PATH!"
        goto git_found
    )
)

:: If Git is still not found, prompt to install automatically via winget
echo [INFO] Git was not detected on your machine.
echo [INFO] Attempting automatic installation via Windows Package Manager (winget)...
where winget >nul 2>nul
if %ERRORLEVEL% equ 0 (
    winget install --id Git.Git -e --source winget --accept-source-agreements --accept-package-agreements
    if exist "C:\Program Files\Git\cmd\git.exe" (
        set "GIT_BIN=C:\Program Files\Git\cmd\git.exe"
        set "PATH=C:\Program Files\Git\cmd;!PATH!"
        goto git_found
    )
)

echo.
echo ===================================================================
echo [ACTION REQUIRED] Git is not installed on this PC.
echo.
echo Please download and install Git (takes 30 seconds):
echo 1. Download installer: https://git-scm.com/download/win
echo 2. Run the installer with default options.
echo 3. Re-run this script (push_to_github.bat).
echo ===================================================================
pause
exit /b 1

:git_found
echo [OK] Using Git: !GIT_BIN!
echo.

:: 2. Initialize Git repository if needed
if not exist ".git" (
    echo [INFO] Initializing Git repository...
    "!GIT_BIN!" init
)

"!GIT_BIN!" branch -M main

:: 3. Configure Remote URL
"!GIT_BIN!" remote remove origin >nul 2>nul
"!GIT_BIN!" remote add origin https://github.com/Ganesh-0411/banknote-authentication-system.git
echo [OK] Remote set to: https://github.com/Ganesh-0411/banknote-authentication-system.git

:: 4. Configure git user if none set
"!GIT_BIN!" config user.name >nul 2>nul
if %ERRORLEVEL% neq 0 (
    "!GIT_BIN!" config user.name "Ganesh-0411"
    "!GIT_BIN!" config user.email "ganesh@users.noreply.github.com"
)

:: 5. Stage and commit
echo.
echo [INFO] Staging all project files (ignoring build artifacts via .gitignore)...
"!GIT_BIN!" add .

echo [INFO] Creating commit...
"!GIT_BIN!" commit -m "Update codebase: zero warnings, comprehensive documentation, and production configurations" --allow-empty

:: 6. Push to GitHub
echo.
echo [INFO] Pushing latest code to GitHub (main branch)...
"!GIT_BIN!" push -u origin main --force

if %ERRORLEVEL% equ 0 (
    echo.
    echo ===================================================================
    echo   [SUCCESS] Code successfully updated on your GitHub repository!
    echo   View online:
    echo   https://github.com/Ganesh-0411/banknote-authentication-system
    echo ===================================================================
) else (
    echo.
    echo [INFO] If GitHub prompted you to sign in via your browser,
    echo        complete the authorization and re-run push_to_github.bat.
)

pause
