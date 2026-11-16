@echo off
rem Modo desarrollo: recarga automatica al guardar.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0windows\desarrollo.ps1"
echo.
pause
