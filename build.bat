@echo off
setlocal enabledelayedexpansion
echo Building S-Emulator Project...

REM Create output directories
if not exist "out\production\client" mkdir "out\production\client"
if not exist "out\production\server" mkdir "out\production\server"
if not exist "out\production\DTO" mkdir "out\production\DTO"
if not exist "out\production\engine" mkdir "out\production\engine"
if not exist "out\production\exception" mkdir "out\production\exception"

REM Find and compile DTO module first (dependency for others)
echo Compiling DTO module...
for /r "DTO\src" %%f in (*.java) do (
    javac -cp "lib\gson-2.11.0.jar" -d "out\production\DTO" "%%f"
    if !errorlevel! neq 0 (
        echo DTO compilation failed on %%f!
        pause
        exit /b 1
    )
)

REM Find and compile Exception module
echo Compiling Exception module...
for /r "exception\src" %%f in (*.java) do (
    javac -cp "lib\gson-2.11.0.jar;out\production\DTO" -d "out\production\exception" "%%f"
    if !errorlevel! neq 0 (
        echo Exception compilation failed on %%f!
        pause
        exit /b 1
    )
)

REM Find and compile Engine module
echo Compiling Engine module...
for /r "engine\src" %%f in (*.java) do (
    javac -cp "lib\gson-2.11.0.jar;out\production\DTO;out\production\exception" -d "out\production\engine" "%%f"
    if !errorlevel! neq 0 (
        echo Engine compilation failed on %%f!
        pause
        exit /b 1
    )
)

REM Find and compile Server module
echo Compiling Server module...
for /r "server\src" %%f in (*.java) do (
    javac -cp "lib\gson-2.11.0.jar;out\production\DTO;out\production\exception;out\production\engine;run\jakarta.xml.bind-api.jar;run\jaxb-core.jar;run\jaxb-impl.jar" -d "out\production\server" "%%f"
    if !errorlevel! neq 0 (
        echo Server compilation failed on %%f!
        pause
        exit /b 1
    )
)

REM Find and compile Client module (JavaFX)
echo Compiling Client module...
for /r "client\src" %%f in (*.java) do (
    javac --module-path "lib\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "lib\gson-2.11.0.jar;out\production\DTO;out\production\exception;out\production\engine;out\production\server" -d "out\production\client" "%%f"
    if !errorlevel! neq 0 (
        echo Client compilation failed on %%f!
        pause
        exit /b 1
    )
)

echo Build completed successfully!
pause