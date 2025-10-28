Write-Host "Preparing S-Emulator WAR deployment..." -ForegroundColor Green

# Create output directory for exploded WAR
$warDir = "out\artifacts\web_SEmulator_Web_exploded"
if (Test-Path $warDir) {
    Remove-Item -Recurse -Force $warDir
}
New-Item -ItemType Directory -Path $warDir -Force | Out-Null

# Copy web content
Write-Host "Copying web content..." -ForegroundColor Yellow
Copy-Item -Recurse "web-SEmulator\web\*" $warDir -Force

# Create WEB-INF structure
$webInfDir = "$warDir\WEB-INF"
$classesDir = "$webInfDir\classes"
$libDir = "$webInfDir\lib"

New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path $libDir -Force | Out-Null

# Copy web.xml
Copy-Item "web-SEmulator\web\WEB-INF\web.xml" $webInfDir -Force

# Copy compiled classes
Write-Host "Copying compiled classes..." -ForegroundColor Yellow
if (Test-Path "out\production\server") {
    Copy-Item -Recurse "out\production\server\*" $classesDir -Force
}
if (Test-Path "out\production\engine") {
    Copy-Item -Recurse "out\production\engine\*" $classesDir -Force
}
if (Test-Path "out\production\DTO") {
    Copy-Item -Recurse "out\production\DTO\*" $classesDir -Force
}
if (Test-Path "out\production\exception") {
    Copy-Item -Recurse "out\production\exception\*" $classesDir -Force
}

# Copy required JARs
Write-Host "Copying required libraries..." -ForegroundColor Yellow
$requiredJars = @(
    "lib\gson-2.11.0.jar",
    "run\jakarta.xml.bind-api.jar",
    "run\jaxb-core.jar", 
    "run\jaxb-impl.jar"
)

foreach ($jar in $requiredJars) {
    if (Test-Path $jar) {
        Copy-Item $jar $libDir -Force
        Write-Host "  Copied: $jar" -ForegroundColor Gray
    } else {
        Write-Host "  Missing: $jar" -ForegroundColor Red
    }
}

Write-Host "WAR deployment prepared at: $warDir" -ForegroundColor Green