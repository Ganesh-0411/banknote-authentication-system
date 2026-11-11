@echo off
setlocal enabledelayedexpansion

echo ===================================================================
echo   BanknoteGuard DL4J - Banknote Authentication System Launcher
echo ===================================================================

cd /d "%~dp0"
if exist "banknote-authentication\pom.xml" (
    cd banknote-authentication
)

:: Ensure standard Windows directories are in PATH
set "PATH=%PATH%;C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0;C:\Windows"

:: 1. Check if Maven is already in PATH
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo [OK] System Maven detected. Starting Spring Boot...
    call mvn spring-boot:run
    goto end
)

:: 2. Check if portable Maven exists
set "PORTABLE_MVN=%USERPROFILE%\.m2\wrapper\apache-maven-3.9.6\bin\mvn.cmd"
if exist "%PORTABLE_MVN%" (
    echo [OK] Portable Maven found at %PORTABLE_MVN%. Starting Spring Boot...
    call "%PORTABLE_MVN%" spring-boot:run
    goto end
)

:: 3. Check other common Maven directories
for %%D in (
    "C:\Program Files\apache-maven*"
    "C:\apache-maven*"
    "C:\tools\apache-maven*"
    "%USERPROFILE%\apache-maven*"
) do (
    for /d %%M in (%%D) do (
        if exist "%%M\bin\mvn.cmd" (
            echo [OK] Found Maven at %%M. Starting Spring Boot...
            call "%%M\bin\mvn.cmd" spring-boot:run
            goto end
        )
    )
)

:: 4. Download Maven to %USERPROFILE%\.m2\wrapper
echo [INFO] Downloading portable Apache Maven 3.9.6...
if not exist "%USERPROFILE%\.m2\wrapper" mkdir "%USERPROFILE%\.m2\wrapper"
set "ZIP_FILE=%USERPROFILE%\.m2\wrapper\maven.zip"
set "WRAPPER_DIR=%USERPROFILE%\.m2\wrapper"

:: Method 1: PowerShell inline with TLS 1.2 (bypasses script policy)
if exist "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe" (
    echo [INFO] Downloading Apache Maven 3.9.6...
    "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe" -ExecutionPolicy Bypass -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $url = 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip'; (New-Object Net.WebClient).DownloadFile($url, '%ZIP_FILE%'); Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%WRAPPER_DIR%' -Force; Remove-Item -Force '%ZIP_FILE%'"
    if exist "%PORTABLE_MVN%" goto start_app
)

:: Method 2: curl.exe with full path + tar.exe
if exist "C:\Windows\System32\curl.exe" (
    echo [INFO] Downloading via curl...
    "C:\Windows\System32\curl.exe" -L -o "%ZIP_FILE%" "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    if exist "%ZIP_FILE%" (
        "C:\Windows\System32\tar.exe" -xf "%ZIP_FILE%" -C "%WRAPPER_DIR%"
        del /f /q "%ZIP_FILE%" >nul 2>nul
    )
    if exist "%PORTABLE_MVN%" goto start_app
)

:: Method 3: certutil.exe with full path
if exist "C:\Windows\System32\certutil.exe" (
    echo [INFO] Downloading via certutil...
    "C:\Windows\System32\certutil.exe" -urlcache -split -f "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" "%ZIP_FILE%"
    if exist "%ZIP_FILE%" (
        if exist "C:\Windows\System32\tar.exe" "C:\Windows\System32\tar.exe" -xf "%ZIP_FILE%" -C "%WRAPPER_DIR%"
        del /f /q "%ZIP_FILE%" >nul 2>nul
    )
    if exist "%PORTABLE_MVN%" goto start_app
)

:start_app
if exist "%PORTABLE_MVN%" (
    echo [OK] Portable Maven configured! Starting Spring Boot Application...
    call "%PORTABLE_MVN%" spring-boot:run
    goto end
)

echo [ERROR] Could not set up Maven automatically.
echo [HELP] In VS Code, open BanknoteAuthenticationApplication.java and click 'Run' above main().
pause
exit /b 1

:end
endlocal
pause
