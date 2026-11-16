# Crea un usuario del personal en Keycloak (con correo verificado y su rol).
. (Join-Path $PSScriptRoot 'comun.ps1')

if (-not (Test-Path $EnvFile)) { Fallar 'Primero ejecuta iniciar.bat.' }
Asegurar-Docker
$cfg = Leer-Env
$keycloak = Id-Contenedor 'keycloak'
if (-not $keycloak) { Fallar 'Keycloak no está en marcha. Ejecuta iniciar.bat.' }

Write-Host ''
$correo = (Read-Host 'Correo del usuario (ej. caja2@polleria.pe)').Trim()
$nombre = (Read-Host 'Nombre (ej. Maria)').Trim()
Write-Host 'Roles: 1) ADMINISTRADOR  2) CAJERO  3) OPERACIONES  4) MESERO'
$opcion = (Read-Host 'Elige el número del rol').Trim()
$roles = @{ '1' = 'ADMINISTRADOR'; '2' = 'CAJERO'; '3' = 'OPERACIONES'; '4' = 'MESERO' }
$rol = $roles[$opcion]
$clave = Read-Host "Contraseña (Enter para usar $ClaveDemo)"

if ($correo -notmatch '^[^@\s]+@[^@\s]+\.[^@\s]+$') { Fallar 'El correo no es válido.' }
if (-not $nombre) { Fallar 'El nombre no puede estar vacío.' }
if (-not $rol) { Fallar 'Rol no válido. Elige 1, 2, 3 o 4.' }
if (-not $clave) { $clave = $ClaveDemo }

docker cp (Join-Path $Raiz 'windows\keycloak\crear-usuarios.sh') "${keycloak}:/tmp/polleria-crear-usuarios.sh"
$env:KC_USER = $cfg['KEYCLOAK_ADMIN_USERNAME']
$env:KC_PASS = $cfg['KEYCLOAK_ADMIN_PASSWORD']
$env:CLAVE_DEMO = $clave
docker exec -e KC_USER -e KC_PASS -e CLAVE_DEMO $keycloak bash /tmp/polleria-crear-usuarios.sh $correo $nombre $rol
$codigo = $LASTEXITCODE
Remove-Item Env:KC_USER, Env:KC_PASS, Env:CLAVE_DEMO -ErrorAction SilentlyContinue
if ($codigo -ne 0) { Fallar 'No se pudo crear el usuario.' }
Ok "Listo. Ya puedes entrar con $correo en $UrlSistema"
