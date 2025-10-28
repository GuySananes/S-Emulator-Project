Write-Host "Building S-Emulator Project..." -ForegroundColor Green

# Create output directories
$dirs = @("out\production\client", "out\production\server", "out\production\DTO", "out\production\engine", "out\production\exception")
foreach ($dir in $dirs) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}

# Function to compile Java files
function Compile-JavaFiles {
    param($SourcePath, $OutputPath, $ClassPath, $ModulePath = "", $AddModules = "")
    
    Write-Host "Compiling $SourcePath..." -ForegroundColor Yellow
    
    $javaFiles = Get-ChildItem -Path $SourcePath -Filter "*.java" -Recurse
    if ($javaFiles.Count -eq 0) {
        Write-Host "No Java files found in $SourcePath" -ForegroundColor Yellow
        return $true
    }
    
    $fileList = ($javaFiles.FullName | ForEach-Object { "`"$_`"" }) -join " "
    
    $cmd = "javac"
    if ($ModulePath) {
        $cmd += " --module-path `"$ModulePath`""
    }
    if ($AddModules) {
        $cmd += " --add-modules $AddModules"
    }
    $cmd += " -cp `"$ClassPath`" -d `"$OutputPath`" $fileList"
    
    Write-Host "Executing: $cmd" -ForegroundColor Gray
    $result = Invoke-Expression $cmd
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation failed for $SourcePath" -ForegroundColor Red
        return $false
    }
    return $true
}

# Compile Exception module first (no dependencies)
$success = Compile-JavaFiles -SourcePath "exception\src" -OutputPath "out\production\exception" -ClassPath "lib\gson-2.11.0.jar"
if (!$success) { exit 1 }

# Compile Engine and DTO together due to circular dependencies
Write-Host "Compiling Engine and DTO modules together..." -ForegroundColor Yellow
$engineDtoClassPath = "lib\gson-2.11.0.jar;out\production\exception;run\jakarta.xml.bind-api.jar;run\jaxb-core.jar;run\jaxb-impl.jar"

# Get all Java files from both modules
$engineFiles = Get-ChildItem -Path "engine\src" -Filter "*.java" -Recurse
$dtoFiles = Get-ChildItem -Path "DTO\src" -Filter "*.java" -Recurse
$allFiles = $engineFiles + $dtoFiles

if ($allFiles.Count -eq 0) {
    Write-Host "No Java files found in engine or DTO modules" -ForegroundColor Yellow
} else {
    $fileList = ($allFiles.FullName | ForEach-Object { "`"$_`"" }) -join " "
    
    # Compile to engine output first, then copy DTO classes to DTO output
    $cmd = "javac -cp `"$engineDtoClassPath`" -d `"out\production\engine`" $fileList"
    Write-Host "Executing: $cmd" -ForegroundColor Gray
    $result = Invoke-Expression $cmd
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation failed for Engine+DTO modules" -ForegroundColor Red
        exit 1
    }
    
    # Copy DTO classes to DTO output directory
    if (Test-Path "out\production\engine\expand") { Copy-Item -Recurse "out\production\engine\expand" "out\production\DTO\" -Force }
    if (Test-Path "out\production\engine\load") { Copy-Item -Recurse "out\production\engine\load" "out\production\DTO\" -Force }
    if (Test-Path "out\production\engine\present") { Copy-Item -Recurse "out\production\engine\present" "out\production\DTO\" -Force }
    if (Test-Path "out\production\engine\run") { Copy-Item -Recurse "out\production\engine\run" "out\production\DTO\" -Force }
    if (Test-Path "out\production\engine\statistic") { Copy-Item -Recurse "out\production\engine\statistic" "out\production\DTO\" -Force }
}

# Compile Server module
$serverClassPath = "lib\gson-2.11.0.jar;lib\tomcat\servlet-api.jar;out\production\DTO;out\production\exception;out\production\engine;run\jakarta.xml.bind-api.jar;run\jaxb-core.jar;run\jaxb-impl.jar"
$success = Compile-JavaFiles -SourcePath "server\src" -OutputPath "out\production\server" -ClassPath $serverClassPath
if (!$success) { exit 1 }

# Compile Client module (JavaFX)
$clientClassPath = "lib\gson-2.11.0.jar;out\production\DTO;out\production\exception;out\production\engine;out\production\server"
$success = Compile-JavaFiles -SourcePath "client\src" -OutputPath "out\production\client" -ClassPath $clientClassPath -ModulePath "lib\javafx-sdk-22.0.2\lib" -AddModules "javafx.controls,javafx.fxml"
if (!$success) { exit 1 }

Write-Host "Build completed successfully!" -ForegroundColor Green