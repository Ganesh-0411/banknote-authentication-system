@echo off
echo ===================================================================
echo   BanknoteGuard DL4J - Training ^& Threshold Calibration CLI
echo ===================================================================
echo Training CNN Autoencoder on Genuine Indian Banknotes...

cd /d "%~dp0"
if exist "banknote-authentication" (
    cd banknote-authentication
)

if exist "mvnw.cmd" (
    call mvnw.cmd compile exec:java -Dexec.mainClass="com.banknote.authentication.training.TrainAutoencoderRunner"
) else if exist "..\mvnw.cmd" (
    call ..\mvnw.cmd compile exec:java -Dexec.mainClass="com.banknote.authentication.training.TrainAutoencoderRunner"
) else (
    call mvn compile exec:java -Dexec.mainClass="com.banknote.authentication.training.TrainAutoencoderRunner"
)

pause
