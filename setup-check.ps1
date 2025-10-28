Write-Host "S-Emulator Project Setup Check" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host ""

# Check Java
Write-Host "Checking Java..." -ForegroundColor Yellow
if ($env:JAVA_HOME) {
    Write-Host "OK JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "ERROR JAVA_HOME not set!" -ForegroundColor Red
}

# Check Tomcat
Write-Host ""
Write-Host "Checking Tomcat..." -ForegroundColor Yellow
$localTomcat = "lib\apache-tomcat-10.1.26"
if (Test-Path "$localTomcat\bin\catalina.bat") {
    Write-Host "OK Local Tomcat: $localTomcat" -ForegroundColor Green
} else {
    Write-Host "ERROR Local Tomcat not found at $localTomcat" -ForegroundColor Red
}

if ($env:CATALINA_HOME) {
    Write-Host "INFO System CATALINA_HOME: $env:CATALINA_HOME" -ForegroundColor Yellow
} else {
    Write-Host "INFO CATALINA_HOME not set (using local Tomcat)" -ForegroundColor Yellow
}

# Check Libraries
Write-Host ""
Write-Host "Checking Libraries..." -ForegroundColor Yellow
$libs = @(
    "lib\gson-2.11.0.jar",
    "lib\javafx-sdk-22.0.2\lib\javafx.controls.jar", 
    "lib\tomcat\servlet-api.jar",
    "run\jakarta.xml.bind-api.jar"
)

foreach ($lib in $libs) {
    if (Test-Path $lib) {
        Write-Host "OK $lib" -ForegroundColor Green
    } else {
        Write-Host "MISSING $lib" -ForegroundColor Red
    }
}

# Check Build Output
Write-Host ""
Write-Host "Checking Build Output..." -ForegroundColor Yellow
$outputs = @(
    "out\production\client",
    "out\production\server",
    "out\production\engine", 
    "out\production\DTO",
    "out\production\exception"
)

foreach ($output in $outputs) {
    if (Test-Path $output) {
        Write-Host "OK $output" -ForegroundColor Green
    } else {
        Write-Host "NOT BUILT $output" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "- Run build.ps1 to compile everything" -ForegroundColor White
Write-Host "- Run run-client.bat for JavaFX desktop app" -ForegroundColor White
Write-Host "- Run run-server-dev.bat for web server" -ForegroundColor White
Write-Host "- Use F5 in VS Code for debugging" -ForegroundColor White