@echo off
rem Crea un usuario del personal en Keycloak.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0windows\crear-usuario.ps1"
echo.
pause
