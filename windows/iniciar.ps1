# Levanta todo el sistema en Windows. Se puede ejecutar todas las veces que haga falta:
# solo hace lo que todavía no está hecho.
. (Join-Path $PSScriptRoot 'comun.ps1')

Write-Host '======================================================' -ForegroundColor Cyan
Write-Host '  Polleria Neyos - inicio del sistema en Windows' -ForegroundColor Cyan
Write-Host '======================================================' -ForegroundColor Cyan

# 1. Docker -------------------------------------------------------------------
Paso '1/7 Comprobando Docker'
Asegurar-Docker

# 2. Configuración (.env.prodtest) --------------------------------------------
Paso '2/7 Configuración (.env.prodtest)'
if (Test-Path $EnvFile) {
    Ok 'Se usa el .env.prodtest existente.'
} else {
    $clave = { [guid]::NewGuid().ToString('N') }
    $contenido = @(
        'COMPOSE_PROJECT_NAME=polleria-prodtest'
        'IMAGE_TAG=prod-test-f19'
        'APP_DOMAIN=127.0.0.1'
        "PUBLIC_APP_URL=$UrlSistema"
        'BIND_ADDRESS=127.0.0.1'
        "POLLERIA_DB_PASSWORD=$(& $clave)"
        "KEYCLOAK_DB_PASSWORD=$(& $clave)"
        'KEYCLOAK_ADMIN_USERNAME=adminpolleria'
        "KEYCLOAK_ADMIN_PASSWORD=$(& $clave)"
    ) -join "`n"
    # UTF-8 sin BOM y saltos de línea de Linux: así lo lee Docker sin problemas.
    [IO.File]::WriteAllText($EnvFile, $contenido + "`n", (New-Object Text.UTF8Encoding $false))
    Ok 'Se creó un .env.prodtest nuevo con contraseñas aleatorias.'
    Aviso 'No lo borres: las bases de datos quedan ligadas a esas contraseñas.'
}
$cfg = Leer-Env
# Copias creadas cuando el sistema usaba Oracle: se agrega la clave de PostgreSQL.
if (-not $cfg['POLLERIA_DB_PASSWORD']) {
    $nueva = [guid]::NewGuid().ToString('N')
    [IO.File]::AppendAllText($EnvFile, "`nPOLLERIA_DB_PASSWORD=$nueva`n", (New-Object Text.UTF8Encoding $false))
    Aviso 'Se agregó POLLERIA_DB_PASSWORD: el sistema ahora usa PostgreSQL en lugar de Oracle.'
    Aviso 'Los datos que estaban en Oracle no se copian: la base nueva empieza vacía.'
    $cfg = Leer-Env
}
foreach ($k in 'COMPOSE_PROJECT_NAME', 'IMAGE_TAG', 'KEYCLOAK_ADMIN_USERNAME', 'KEYCLOAK_ADMIN_PASSWORD', 'POLLERIA_DB_PASSWORD') {
    if (-not $cfg[$k]) { Fallar "Falta la variable $k en .env.prodtest." }
}
if ($cfg['PUBLIC_APP_URL'] -ne $UrlSistema) {
    Aviso "PUBLIC_APP_URL en .env.prodtest es '$($cfg['PUBLIC_APP_URL'])'. Para esta copia debe ser $UrlSistema."
}

# 3. Configuración del login (realm de Keycloak) ------------------------------
Paso '3/7 Configuración del login (Keycloak)'
$destinoRealm = Join-Path $Raiz "production\generated\$($cfg['COMPOSE_PROJECT_NAME'])"
$archivoRealm = Join-Path $destinoRealm 'polleria-realm.json'
if (Test-Path $archivoRealm) {
    Ok 'Ya existe.'
} else {
    New-Item -ItemType Directory -Force $destinoRealm | Out-Null
    Copy-Item (Join-Path $Raiz 'windows\keycloak\polleria-realm-local.json') $archivoRealm
    Ok 'Copiada la configuración local.'
}

# 4. Imágenes de Docker -------------------------------------------------------
Paso '4/7 Imágenes de Docker'
function Imagenes-Listas {
    docker image inspect "polleria-backend:$($cfg['IMAGE_TAG'])" "polleria-frontend:$($cfg['IMAGE_TAG'])" *> $null
    return ($LASTEXITCODE -eq 0)
}
$compilar = $false
if (Imagenes-Listas) {
    Ok 'Las imágenes del backend y del frontend ya están cargadas.'
} else {
    $tar = Join-Path $Raiz 'polleria-imagenes.tar'
    if (Test-Path $tar) {
        Ok 'Cargando imágenes desde polleria-imagenes.tar (puede tardar unos minutos)...'
        docker load -i $tar
    }
    if (Imagenes-Listas) {
        Ok 'Imágenes cargadas.'
    } else {
        $compilar = $true
        Aviso 'No hay imágenes listas: se compilarán el backend y el frontend.'
        Aviso 'La primera vez puede tardar 10 minutos o más y necesita internet.'
    }
}

# 5. PostgreSQL ---------------------------------------------------------------
Paso '5/7 Base de datos PostgreSQL'
# --remove-orphans quita el contenedor de Oracle de versiones anteriores (su volumen se conserva).
Invoke-Compose up -d --remove-orphans postgres
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo iniciar PostgreSQL.' }
$pg = Id-Contenedor 'postgres'
if (-not $pg) { Fallar 'No se encontró el contenedor de PostgreSQL.' }
Write-Host '    Esperando a que PostgreSQL esté listo' -NoNewline
$estado = ''
for ($i = 0; $i -lt 60; $i++) {
    $estado = docker inspect -f '{{.State.Health.Status}}' $pg
    if ($estado -eq 'healthy') { break }
    Start-Sleep -Seconds 2
    Write-Host '.' -NoNewline
}
Write-Host ''
if ($estado -ne 'healthy') { Fallar "PostgreSQL no quedó listo (estado: $estado)." }
Ok 'PostgreSQL está listo.'

# 6. Tablas -------------------------------------------------------------------
Paso '6/7 Tablas de la base de datos (pollería y clínica)'
docker exec $pg rm -rf /tmp/polleria-db
docker exec $pg mkdir -p /tmp/polleria-db
docker cp (Join-Path $Raiz 'polleria-backend\scripts') "${pg}:/tmp/polleria-db/"
docker cp (Join-Path $Raiz 'windows\postgres\crear-tablas.sh') "${pg}:/tmp/polleria-db/crear-tablas.sh"
$salida = docker exec $pg sh /tmp/polleria-db/crear-tablas.sh
$codigo = $LASTEXITCODE
$salida | ForEach-Object { Write-Host "    $_" }
if ($codigo -ne 0) { Fallar 'No se pudieron crear las tablas. Revisa los mensajes de arriba.' }
if ($salida -contains 'TABLAS_EXISTENTES') { Ok 'Las tablas ya existían.' } else { Ok 'Tablas creadas.' }

# 7. Resto del sistema --------------------------------------------------------
Paso '7/7 Keycloak, backend y frontend'
if ($compilar) {
    Invoke-Compose up -d --build --wait --wait-timeout 1200
} else {
    Invoke-Compose up -d --wait --wait-timeout 900
}
if ($LASTEXITCODE -ne 0) {
    Invoke-Compose ps
    Fallar 'Algún servicio no quedó listo. Revisa sus registros con: docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml logs --tail 100 backend'
}
Ok 'Todos los servicios están listos.'

# Usuarios de prueba ----------------------------------------------------------
Paso 'Usuarios de prueba'
$keycloak = Id-Contenedor 'keycloak'
docker cp (Join-Path $Raiz 'windows\keycloak\crear-usuarios.sh') "${keycloak}:/tmp/polleria-crear-usuarios.sh"
$env:KC_USER = $cfg['KEYCLOAK_ADMIN_USERNAME']
$env:KC_PASS = $cfg['KEYCLOAK_ADMIN_PASSWORD']
$env:CLAVE_DEMO = $ClaveDemo
docker exec -e KC_USER -e KC_PASS -e CLAVE_DEMO $keycloak bash /tmp/polleria-crear-usuarios.sh
$codigo = $LASTEXITCODE
Remove-Item Env:KC_USER, Env:KC_PASS, Env:CLAVE_DEMO -ErrorAction SilentlyContinue
if ($codigo -ne 0) {
    Aviso 'No se pudieron crear todos los usuarios. El sistema funciona; créalos con crear-usuario.bat.'
}

# Listo -----------------------------------------------------------------------
Write-Host ''
Write-Host '======================================================' -ForegroundColor Green
Write-Host "  Sistema listo en $UrlSistema" -ForegroundColor Green
Write-Host '======================================================' -ForegroundColor Green
Write-Host "  Usuarios de prueba (contraseña: $ClaveDemo)"
Write-Host '    admin@polleria.pe   -> ADMINISTRADOR'
Write-Host '    caja@polleria.pe    -> CAJERO'
Write-Host '    cocina@polleria.pe  -> OPERACIONES'
Write-Host '    mesero@polleria.pe  -> MESERO'
Write-Host ''
Write-Host '  Para apagar: detener.bat'
Start-Process $UrlSistema
