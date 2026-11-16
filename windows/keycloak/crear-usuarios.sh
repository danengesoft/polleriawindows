#!/bin/bash
# Se ejecuta DENTRO del contenedor de Keycloak (lo copia y lanza iniciar.ps1).
# Crea los usuarios de prueba de cada rol solo si no existen.
# Con 3 argumentos (correo nombre ROL) crea solo ese usuario.
# Recibe por variables de entorno: KC_USER, KC_PASS (administrador de Keycloak) y CLAVE_DEMO.
set -u
KC=/opt/keycloak/bin/kcadm.sh
CFG=/tmp/polleria-kcadm.config

$KC config credentials --config "$CFG" --server http://localhost:8080/auth --realm master \
  --user "$KC_USER" --password "$KC_PASS" >/dev/null || { echo "FALLO_LOGIN_KEYCLOAK"; exit 1; }

crear() {
  local correo=$1 nombre=$2 rol=$3
  if $KC get users --config "$CFG" -r polleria -q exact=true -q username="$correo" --fields id | grep -q '"id"'; then
    echo "Ya existe: $correo ($rol)"
    return 0
  fi
  $KC create users --config "$CFG" -r polleria -s username="$correo" -s email="$correo" \
    -s emailVerified=true -s enabled=true -s firstName="$nombre" -s lastName=Neyos >/dev/null || return 1
  $KC set-password --config "$CFG" -r polleria --username "$correo" --new-password "$CLAVE_DEMO" || return 1
  $KC add-roles --config "$CFG" -r polleria --uname "$correo" --rolename "$rol" || return 1
  echo "Creado: $correo ($rol)"
}

error=0
if [ $# -ge 3 ]; then
  # Uso: crear-usuarios.sh correo nombre ROL  (un solo usuario)
  crear "$1" "$2" "$3" || error=1
else
  crear admin@polleria.pe Admin ADMINISTRADOR || error=1
  crear caja@polleria.pe Caja CAJERO || error=1
  crear cocina@polleria.pe Cocina OPERACIONES || error=1
  crear mesero@polleria.pe Mesero MESERO || error=1
fi
rm -f "$CFG"

if [ $error -ne 0 ]; then echo "FALLO_USUARIOS"; exit 1; fi
echo "USUARIOS_OK"
