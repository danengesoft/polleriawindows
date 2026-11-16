# Apaga todo el sistema. Los datos (base de datos, usuarios, fotos) se conservan.
. (Join-Path $PSScriptRoot 'comun.ps1')

Paso 'Apagando el sistema'
if (-not (Test-Path $EnvFile)) { Fallar 'No existe .env.prodtest: el sistema nunca se inició en esta carpeta.' }
Asegurar-Docker
Invoke-Compose -f compose.live.yml down --remove-orphans
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo apagar el sistema.' }
Ok 'Sistema apagado. Los datos se conservan; para volver a usarlo ejecuta iniciar.bat.'
