@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Batch Script for Windows
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "DIRNAME=%~dp0"
if "%DIRNAME%" == "" set "DIRNAME=."

@REM 1. Check if 'mvn' is available in system PATH
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    goto end
)

@REM 2. Check portable Maven wrapper
set "MVN_VERSION=3.9.6"
set "MVN_DIR=%USERPROFILE%\.m2\wrapper\apache-maven-%MVN_VERSION%"
set "MAVEN_EXEC=%MVN_DIR%\bin\mvn.cmd"
if exist "%MAVEN_EXEC%" goto run_maven

@REM 3. Check common Maven install locations
for %%D in (
    "C:\Program Files\apache-maven*"
    "C:\Program Files (x86)\apache-maven*"
    "C:\apache-maven*"
    "C:\tools\apache-maven*"
    "%USERPROFILE%\apache-maven*"
) do (
    for /d %%M in (%%D) do (
        if exist "%%M\bin\mvn.cmd" (
            set "MAVEN_EXEC=%%M\bin\mvn.cmd"
            goto run_maven
        )
    )
)

@REM 4. Download and setup portable Maven via Java DownloadMaven.java
echo [Maven Wrapper] Apache Maven not detected in PATH.
echo [Maven Wrapper] Downloading portable Maven %MVN_VERSION% via Java...

if exist "%DIRNAME%DownloadMaven.java" (
    java "%DIRNAME%DownloadMaven.java"
)

if exist "%MAVEN_EXEC%" goto run_maven

@REM 5. Fallback via PowerShell
echo [Maven Wrapper] Trying PowerShell WebClient download fallback...
powershell -ExecutionPolicy Bypass -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $dir = [System.IO.Path]::Combine($env:USERPROFILE, '.m2\wrapper'); New-Item -ItemType Directory -Force -Path $dir | Out-Null; $zip = [System.IO.Path]::Combine($dir, 'maven.zip'); (New-Object System.Net.WebClient).DownloadFile('https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip', $zip); Expand-Archive -Path $zip -DestinationPath $dir -Force; Remove-Item -Force $zip"

if exist "%MAVEN_EXEC%" goto run_maven

echo [ERROR] Failed to configure Maven automatically.
echo Please run the application directly from your IDE by opening BanknoteAuthenticationApplication.java and clicking Run.
pause
exit /b 1

:run_maven
call "%MAVEN_EXEC%" %*

:end
endlocal
