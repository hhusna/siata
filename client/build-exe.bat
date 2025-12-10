@echo off
echo ========================================
echo SIATA Client - Build Windows Executable
echo ========================================
echo.

REM Step 1: Build fat JAR
echo [1/3] Building fat JAR with all dependencies...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo.

REM Step 2: Check if jpackage is available
echo [2/3] Checking jpackage availability...
where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: jpackage not found! Make sure JDK 21 is installed and in PATH.
    echo You can still run the JAR with: java -jar target\client-1.0-SNAPSHOT.jar
    pause
    exit /b 1
)
echo.

REM Step 3: Create Windows EXE with jpackage
echo [3/3] Creating Windows executable with jpackage...
if exist "target\siata-installer" rmdir /s /q "target\siata-installer"

jpackage ^
    --type exe ^
    --input target ^
    --name SIATA ^
    --main-jar client-1.0-SNAPSHOT.jar ^
    --main-class com.siata.client.Launcher ^
    --dest target\siata-installer ^
    --app-version 1.0.0 ^
    --vendor "Direktorat Angkutan Udara" ^
    --description "Sistem Informasi Aset TI Angkutan Udara" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-shortcut-prompt

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: jpackage failed!
    echo.
    echo Alternative: You can use the fat JAR directly:
    echo   java -jar target\client-1.0-SNAPSHOT.jar
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD COMPLETE!
echo ========================================
echo.
echo Executable installer created at:
echo   target\siata-installer\SIATA-1.0.0.exe
echo.
echo You can also run the JAR directly with:
echo   java -jar target\client-1.0-SNAPSHOT.jar
echo.
pause
