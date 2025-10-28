@echo off
echo Starting S-Emulator Tomcat Development Server...
echo.

REM Use local Tomcat installation
set "LOCAL_CATALINA_HOME=%~dp0lib\apache-tomcat-10.1.26"
echo Using local Tomcat: %LOCAL_CATALINA_HOME%
echo JAVA_HOME: %JAVA_HOME%
echo.

if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set!
    echo Please set JAVA_HOME to your Java installation directory
    pause
    exit /b 1
)

if not exist "%LOCAL_CATALINA_HOME%\bin\catalina.bat" (
    echo ERROR: Local Tomcat not found at %LOCAL_CATALINA_HOME%
    pause
    exit /b 1
)

REM Build the project first
echo Building project...
powershell -ExecutionPolicy Bypass -File build.ps1
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

REM Prepare WAR deployment
echo Preparing WAR deployment...
powershell -ExecutionPolicy Bypass -File deploy-war.ps1

REM Copy web application to Tomcat webapps
echo Deploying web application...
if exist "out\artifacts\web_SEmulator_Web_exploded" (
    if exist "%LOCAL_CATALINA_HOME%\webapps\web_SEmulator_Web_exploded" (
        rmdir /s /q "%LOCAL_CATALINA_HOME%\webapps\web_SEmulator_Web_exploded"
    )
    xcopy /e /i /y "out\artifacts\web_SEmulator_Web_exploded" "%LOCAL_CATALINA_HOME%\webapps\web_SEmulator_Web_exploded"
    echo Web application deployed successfully
) else (
    echo Warning: Exploded WAR not found, you may need to build it manually
)

echo.
echo Starting Tomcat server...
echo Access your application at: http://localhost:8080/web_SEmulator_Web_exploded
echo Press Ctrl+C to stop the server
echo.

REM Save current directory
set "ORIGINAL_DIR=%CD%"

REM Set Tomcat environment and start server
set "CATALINA_HOME=%LOCAL_CATALINA_HOME%"
set "CATALINA_BASE=%LOCAL_CATALINA_HOME%"

REM Start Tomcat from original directory to preserve working directory context
"%LOCAL_CATALINA_HOME%\bin\catalina.bat" run