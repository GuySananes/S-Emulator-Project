Write-Host "Debugging S-Emulator Server Environment..." -ForegroundColor Green

# Check current working directory
Write-Host "Current Working Directory: $(Get-Location)" -ForegroundColor Yellow

# Check Java system properties that might affect file loading
Write-Host ""
Write-Host "Java System Properties:" -ForegroundColor Yellow
Write-Host "java.io.tmpdir: $env:TEMP" -ForegroundColor Gray
Write-Host "user.dir would be: $(Get-Location)" -ForegroundColor Gray

# Check if resources are accessible
Write-Host ""
Write-Host "Checking Resources:" -ForegroundColor Yellow
$resourcesPath = "out\artifacts\web_SEmulator_Web_exploded\WEB-INF\classes\resources"
if (Test-Path $resourcesPath) {
    Write-Host "✓ Resources found at: $resourcesPath" -ForegroundColor Green
    $xmlFiles = Get-ChildItem "$resourcesPath\*.xml"
    Write-Host "  XML files: $($xmlFiles.Count)" -ForegroundColor Gray
    foreach ($file in $xmlFiles) {
        Write-Host "    - $($file.Name)" -ForegroundColor Gray
    }
} else {
    Write-Host "✗ Resources not found at: $resourcesPath" -ForegroundColor Red
}

# Check temp directory
Write-Host ""
Write-Host "Temp Directory Contents:" -ForegroundColor Yellow
$tempDir = "$env:TEMP\semulator-uploads"
if (Test-Path $tempDir) {
    Write-Host "✓ Upload temp dir exists: $tempDir" -ForegroundColor Green
    $uploadFiles = Get-ChildItem $tempDir -ErrorAction SilentlyContinue
    Write-Host "  Files: $($uploadFiles.Count)" -ForegroundColor Gray
} else {
    Write-Host "✗ Upload temp dir not found: $tempDir" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Recommendation: Check if IntelliJ sets a different working directory" -ForegroundColor Cyan