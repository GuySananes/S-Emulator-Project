@echo off
echo Starting S-Emulator JavaFX Client...

REM Check if compiled
if not exist "out\production\client\javafxUI\Main.class" (
    echo Client not compiled. Running build first...
    powershell -ExecutionPolicy Bypass -File build.ps1
    if %errorlevel% neq 0 (
        echo Build failed!
        pause
        exit /b 1
    )
)

REM Run JavaFX Client
java --module-path "lib\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "out\production\client;out\production\DTO;out\production\exception;out\production\engine;out\production\server;lib\gson-2.11.0.jar" javafxUI.Main

pause