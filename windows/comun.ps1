# Funciones compartidas por los scripts de Windows. No se ejecuta solo.
$ErrorActionPreference = 'Continue'
$Raiz = Split-Path -Parent $PSScriptRoot
$EnvFile = Join-Path $Raiz '.env.prodtest'
$UrlSistema = 'http://127.0.0.1:18080'
$ClaveDemo = 'Neyos2026!'
Set-Location $Raiz

function Paso([string]$texto) {
    Write-Host ''
    Write-Host "==> $texto" -ForegroundColor Cyan
}

function Ok([string]$texto) {
    Write-Host "    $texto" -ForegroundColor Green
}

function Aviso([string]$texto) {
    Write-Host "    $texto" -ForegroundColor Yellow
}

function Fallar([string]$texto) {
    Write-Host ''
    Write-Host "ERROR: $texto" -ForegroundColor Red
    Write-Host 'Consulta la sección "Problemas comunes" de LEEME-WINDOWS.md.' -ForegroundColor Red
    exit 1
}

# docker compose con el archivo de entorno y los archivos base del proyecto.
function Invoke-Compose {
    & docker compose --env-file $EnvFile -f compose.prod.yml -f compose.prodtest.yml @args
}

function Leer-Env {
    $valores = @{}
    if (Test-Path $EnvFile) {
        foreach ($linea in Get-Content $EnvFile) {
            if ($linea -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
                $valores[$Matches[1]] = $Matches[2].Trim()
            }
        }
    }
    return $valores
}

function Docker-Responde {
    docker info --format '{{.ServerVersion}}' *> $null
    return ($LASTEXITCODE -eq 0)
}

function Asegurar-Docker {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Fallar 'Docker no está instalado. Instala Docker Desktop y vuelve a ejecutar este script.'
    }
    if (-not (Docker-Responde)) {
        $exe = Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'
        if (Test-Path $exe) {
            Aviso 'Docker Desktop no está abierto. Iniciándolo...'
            Start-Process $exe
        } else {
            Aviso 'Abre Docker Desktop manualmente.'
        }
        Write-Host '    Esperando a Docker (hasta 3 minutos)' -NoNewline
        for ($i = 0; $i -lt 36; $i++) {
            if (Docker-Responde) { break }
            Start-Sleep -Seconds 5
            Write-Host '.' -NoNewline
        }
        Write-Host ''
        if (-not (Docker-Responde)) {
            Fallar 'Docker no responde. Abre Docker Desktop, espera a "Engine running" y vuelve a intentarlo.'
        }
    }
    $version = (docker compose version --short) | Select-Object -First 1
    if ($version -match '(\d+)\.(\d+)') {
        $mayor = [int]$Matches[1]; $menor = [int]$Matches[2]
        if ($mayor -lt 2 -or ($mayor -eq 2 -and $menor -lt 24)) {
            Fallar "Docker Compose $version es muy antiguo. Actualiza Docker Desktop (se necesita 2.24 o superior)."
        }
    }
    Ok 'Docker está funcionando.'
}

function Id-Contenedor([string]$servicio) {
    return (Invoke-Compose ps -q $servicio | Select-Object -First 1)
}
