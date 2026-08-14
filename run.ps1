# Double-click / terminal launcher for TARUMT Hotel Management System
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# UTF-8 + ANSI colors so CLI boxes and word colors render on Windows
try {
  chcp 65001 | Out-Null
  $OutputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
  $code = @"
using System;
using System.Runtime.InteropServices;
public static class WinVt {
  [DllImport("kernel32.dll")] public static extern IntPtr GetStdHandle(int n);
  [DllImport("kernel32.dll")] public static extern bool GetConsoleMode(IntPtr h, out uint m);
  [DllImport("kernel32.dll")] public static extern bool SetConsoleMode(IntPtr h, uint m);
}
"@
  Add-Type -TypeDefinition $code -ErrorAction SilentlyContinue
  $handle = [WinVt]::GetStdHandle(-11)
  $mode = 0
  [void][WinVt]::GetConsoleMode($handle, [ref]$mode)
  [void][WinVt]::SetConsoleMode($handle, ($mode -bor 4))
} catch {
  # ignore if console encoding / ANSI cannot be changed
}

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
$argFile = Join-Path $PSScriptRoot "bin\sources.txt"
$files.FullName | Set-Content -Path $argFile -Encoding ascii
& $javac -d bin -encoding UTF-8 "@$argFile"
if ($LASTEXITCODE -ne 0) {
  Write-Host "Compile failed."
  Read-Host "Press Enter to close"
  exit 1
}

Write-Host "Starting hotel menu..."
& $java "-Dfile.encoding=UTF-8" -cp bin app.HotelMain
Read-Host "Press Enter to close"
