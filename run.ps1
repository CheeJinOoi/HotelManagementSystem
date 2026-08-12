# Double-click / terminal launcher for TARUMT Hotel Management System
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$javaHome = "C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot"
$java = Join-Path $javaHome "bin\java.exe"
$javac = Join-Path $javaHome "bin\javac.exe"

if (-not (Test-Path $java)) {
  $javaCmd = Get-Command java -ErrorAction SilentlyContinue
  $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
  if (-not $javaCmd -or -not $javacCmd) {
    Write-Host "Java was not found. Install JDK 17 first."
    Read-Host "Press Enter to close"
    exit 1
  }
  $java = $javaCmd.Source
  $javac = $javacCmd.Source
}

New-Item -ItemType Directory -Force -Path bin, data | Out-Null

$files = Get-ChildItem -Path "src" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
if ($files.Count -eq 0) {
  Write-Host "No Java source files found in src/."
  Read-Host "Press Enter to close"
  exit 1
}

Write-Host "Compiling..."
& $javac -d bin -encoding UTF-8 ($files.FullName)
if ($LASTEXITCODE -ne 0) {
  Write-Host "Compile failed."
  Read-Host "Press Enter to close"
  exit 1
}

Write-Host "Starting hotel menu..."
& $java -cp bin app.HotelMain
Read-Host "Press Enter to close"
