# Funciones compartidas por los scripts de Windows. No se ejecuta solo.
$ErrorActionPreference = 'Continue'
$Raiz = Split-Path -Parent $PSScriptRoot
$EnvFile = Join-Path $Raiz '.env.prodtest'
$PuertoSistema = 18080
$UrlSistema = "http://127.0.0.1:$PuertoSistema"
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

# Revisa que el puerto del sistema esté libre antes de arrancar. Si lo ocupa un
# contenedor de otro proyecto (otra copia de la pollería, medmonitor, ...) ofrece
# apagarlo; si lo ocupa un programa de Windows, dice cuál es.
function Liberar-Puerto([int]$puerto, [string]$proyecto) {
    $ocupantes = docker ps --filter "publish=$puerto" --format '{{.Names}}|{{.Labels}}'
    foreach ($linea in @($ocupantes)) {
        if (-not $linea) { continue }
        $nombre, $etiquetas = $linea -split '\|', 2
        $dueno = if ($etiquetas -match 'com\.docker\.compose\.project=([^,]+)') { $Matches[1] } else { '' }
        if ($dueno -eq $proyecto) { continue }   # es nuestro propio frontend
        $origen = if ($dueno) { " (proyecto '$dueno')" } else { '' }
        Aviso "El puerto $puerto lo está usando el contenedor '$nombre'$origen."
        $respuesta = Read-Host '    ¿Apagarlo para usar la pollería? (S/N)'
        if ($respuesta -notmatch '^\s*[sS]') {
            Fallar "El puerto $puerto está ocupado por '$nombre'. Apágalo con: docker stop $nombre"
        }
        docker stop $nombre | Out-Null
        docker update --restart=no $nombre | Out-Null
        Ok "Se apagó '$nombre' y ya no se encenderá solo. Para volver a usarlo: docker start $nombre"
    }
    if (Get-Command Get-NetTCPConnection -ErrorAction SilentlyContinue) {
        $escucha = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($escucha) {
            $proceso = Get-Process -Id $escucha.OwningProcess -ErrorAction SilentlyContinue
            $nombreProceso = if ($proceso) { $proceso.ProcessName } else { 'desconocido' }
            # Los procesos de Docker Desktop publican los puertos de los contenedores: no son un conflicto.
            if ($nombreProceso -notmatch '^(com\.docker|docker|wslrelay|vpnkit)') {
                Fallar "El puerto $puerto lo usa el programa '$nombreProceso' (PID $($escucha.OwningProcess)). Ciérralo y vuelve a ejecutar iniciar.bat."
            }
        }
    }
    Ok "El puerto $puerto está disponible para la pollería."
}

# Si el frontend arrancó sin su puerto publicado (pasa tras un choque de puertos),
# lo vuelve a crear para que la web responda en $UrlSistema.
function Asegurar-PuertoFrontend {
    $frontend = Id-Contenedor 'frontend'
    if (-not $frontend) { return }
    $publicado = docker port $frontend 80 2>$null
    if ($publicado) { return }
    Aviso 'El frontend quedó sin su puerto publicado. Se vuelve a crear...'
    Invoke-Compose up -d --force-recreate --no-deps --wait frontend
    if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo volver a crear el frontend.' }
    Ok 'Frontend publicado otra vez.'
}
