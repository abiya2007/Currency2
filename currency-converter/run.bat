@echo off
title Currency Converter Java Server Launcher
cd /d "%~dp0"

echo Running Currency Converter Java Server...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Script ended with an error code %ERRORLEVEL%.
    pause
)
