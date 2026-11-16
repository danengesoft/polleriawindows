@echo off
rem Apaga el sistema. Los datos se conservan.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0windows\detener.ps1"
echo.
pause
