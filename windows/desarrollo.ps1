# Modo desarrollo: los cambios del frontend se ven al guardar y el backend se
# reconstruye solo.
. (Join-Path $PSScriptRoot 'comun.ps1')

if (-not (Test-Path $EnvFile)) { Fallar 'Primero ejecuta iniciar.bat.' }
Asegurar-Docker

Paso 'Levantando el frontend de desarrollo (la primera vez descarga dependencias)'
Invoke-Compose -f compose.live.yml up -d --build --no-deps frontend-dev frontend
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo levantar el frontend de desarrollo.' }

Write-Host ''
Write-Host "  Recarga automática activa en $UrlSistema" -ForegroundColor Green
Write-Host '  - Frontend: guarda un archivo de polleria-frontend\src y el navegador se actualiza.' -ForegroundColor Green
Write-Host '  - Backend: guarda un archivo de polleria-backend\src\main y se reconstruye en 1-2 minutos.' -ForegroundColor Green
Write-Host '  Deja esta ventana abierta. Para salir: Ctrl + C.' -ForegroundColor Green
Write-Host ''
Invoke-Compose -f compose.live.yml -f compose.watch.yml watch --no-up --prune=false backend
