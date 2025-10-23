# Prepares the exploded WAR for Tomcat by copying compiled classes and libs into WEB-INF
# Usage: Right-click this file in Explorer and Run with PowerShell (or run in terminal):
#   powershell -ExecutionPolicy Bypass -File run/prepare_exploded_war.ps1

$ErrorActionPreference = 'Stop'

# Resolve paths
$root = Split-Path -Parent $PSScriptRoot
$exploded = Join-Path $root 'out\artifacts\web_SEmulator_Web_exploded'
$webinf = Join-Path $exploded 'WEB-INF'
$classesTarget = Join-Path $webinf 'classes'
$libTarget = Join-Path $webinf 'lib'

Write-Host "[prepare] Project root: $root"
Write-Host "[prepare] Exploded WAR: $exploded"

if (!(Test-Path $exploded)) {
  throw "Exploded WAR not found at $exploded. Build the 'web-SEmulator: Web exploded' artifact first."
}

# Ensure target folders exist
New-Item -ItemType Directory -Force -Path $classesTarget | Out-Null
New-Item -ItemType Directory -Force -Path $libTarget | Out-Null

# Helper to copy if source exists
function CopyTreeIfExists($src, $dst) {
  if (Test-Path $src) {
    Write-Host "[copy] $src -> $dst"
    Copy-Item -Recurse -Force -Path $src -Destination $dst
  } else {
    Write-Host "[skip] Missing: $src"
  }
}

# Copy server classes (servlets)
$serverOut = Join-Path $root 'out\production\server\sserver'
CopyTreeIfExists $serverOut (Join-Path $classesTarget 'sserver')

# Copy engine, DTO, and exception compiled classes used by the server
$engineOut = Join-Path $root 'out\production\engine'
$dtoOut    = Join-Path $root 'out\production\DTO'
$excOut    = Join-Path $root 'out\production\exception'

CopyTreeIfExists $engineOut $classesTarget
CopyTreeIfExists $dtoOut    $classesTarget
CopyTreeIfExists $excOut    $classesTarget

# Copy 3rd-party libs required at runtime (Gson)
$gsonJar = Join-Path $root 'lib\gson-2.11.0.jar'
if (Test-Path $gsonJar) {
  Write-Host "[lib] + gson-2.11.0.jar"
  Copy-Item -Force -Path $gsonJar -Destination (Join-Path $libTarget 'gson-2.11.0.jar')
} else {
  Write-Warning "gson-2.11.0.jar not found under lib/. Add it or update this script."
}

Write-Host "[done] Exploded WAR enriched. You can now deploy the directory:`n  $exploded`ninto Tomcat's webapps folder (as an exploded app), or zip it into a WAR."