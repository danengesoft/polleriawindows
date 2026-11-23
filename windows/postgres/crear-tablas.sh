#!/bin/sh
# Se ejecuta DENTRO del contenedor de PostgreSQL (lo copia y lanza iniciar.ps1).
# Crea los esquemas solo si todavía no existen. Es seguro ejecutarlo varias veces.
#   polleria     -> tablas del sistema (01-polleria.sql)
#   exa_clinica  -> módulo clínica del examen v7 (02-clinica.sql)
set -u
DIR=/tmp/polleria-db/scripts
PSQL="psql -U polleria -d polleria -v ON_ERROR_STOP=1 -q"

existe() { [ "$($PSQL -Atc "SELECT to_regclass('$1') IS NOT NULL")" = "t" ]; }
esquema() { [ "$($PSQL -Atc "SELECT count(*) FROM information_schema.schemata WHERE schema_name='$1'")" = "1" ]; }

aplicar() {
  echo "Aplicando $1"
  $PSQL -1 -f "$DIR/$1" || { echo "FALLO_SCRIPT $1"; exit 1; }
}

if existe polleria.web_pedidos; then
  echo "TABLAS_EXISTENTES"
elif esquema polleria; then
  echo "TABLAS_INCOMPLETAS: el esquema polleria tiene solo una parte de las tablas. Revisar a mano."
  exit 2
else
  aplicar 01-polleria.sql
  echo "TABLAS_CREADAS"
fi

if existe exa_clinica.consultorio_especialidad; then
  echo "CLINICA_EXISTENTE"
elif esquema exa_clinica; then
  echo "CLINICA_INCOMPLETA: el esquema exa_clinica está a medias. Bórralo con DROP SCHEMA EXA_CLINICA CASCADE; y vuelve a iniciar."
  exit 2
else
  aplicar 02-clinica.sql
  echo "CLINICA_CREADA"
fi
