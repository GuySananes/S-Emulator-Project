@echo off
echo Compiling and running server tests...
echo.

REM Create output directory for test classes
if not exist "out\test" mkdir out\test

REM Compile test classes
echo Compiling SessionIsolationTest...
javac -cp "out\production\server;out\production\engine;out\production\DTO;out\production\exception;lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar" -d out\test server\test\sserver\test\SessionIsolationTest.java

if %ERRORLEVEL% NEQ 0 (
    echo Failed to compile SessionIsolationTest
    exit /b 1
)

echo Compiling ExecutionWorkflowTest...
javac -cp "out\production\server;out\production\engine;out\production\DTO;out\production\exception;lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar" -d out\test server\test\sserver\test\ExecutionWorkflowTest.java

if %ERRORLEVEL% NEQ 0 (
    echo Failed to compile ExecutionWorkflowTest
    exit /b 1
)

echo Compiling ErrorHandlingTest...
javac -cp "out\production\server;out\production\engine;out\production\DTO;out\production\exception;lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar" -d out\test server\test\sserver\test\ErrorHandlingTest.java

if %ERRORLEVEL% NEQ 0 (
    echo Failed to compile ErrorHandlingTest
    exit /b 1
)

echo.
echo ========================================
echo Running SessionIsolationTest...
echo ========================================
java -cp "out\test;out\production\server;out\production\engine;out\production\DTO;out\production\exception;lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar;lib\mod\*" sserver.test.SessionIsolationTest

if %ERRORLEVEL% NEQ 0 (
    echo SessionIsolationTest failed
    exit /b 1
)

echo.
echo ========================================
echo Running ExecutionWorkflowTest...
echo ========================================
java -cp "out\test;out\production\server;out\production\engine;out\production\DTO;out\production\exception;lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar;lib\mod\*" sserver.test.ExecutionWorkflowTest

if %ERRORLEVEL% NEQ 0 (
    echo ExecutionWorkflowTest failed
    exit /b 1
)

echo.
echo ========================================
echo Running ErrorHandlingTest...
echo ========================================
java -cp "out\test;out\production\server;out\production\engine;out\production\DTO;out\production\exception;lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar;lib\mod\*" sserver.test.ErrorHandlingTest

if %ERRORLEVEL% NEQ 0 (
    echo ErrorHandlingTest failed
    exit /b 1
)

echo.
echo ========================================
echo All tests completed successfully!
echo ========================================
