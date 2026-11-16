#!/bin/bash
# Se ejecuta DENTRO del contenedor de Oracle (lo copia y lanza iniciar.ps1).
# Crea las tablas solo si todavía no existen. Es seguro ejecutarlo varias veces.
set -u
DIR=/tmp/polleria-oracle/scripts

contar_tabla() {
  printf "SET HEADING OFF FEEDBACK OFF PAGESIZE 0\nALTER SESSION SET CONTAINER=FREEPDB1;\nSELECT COUNT(*) FROM DBA_TABLES WHERE OWNER='POLLERIA' AND TABLE_NAME='%s';\nEXIT\n" "$1" \
    | sqlplus -s / as sysdba | tr -d '[:space:]'
}

if [ "$(contar_tabla PRESENTACIONES_PRODUCTO)" = "1" ]; then
  echo "TABLAS_EXISTENTES"
  exit 0
fi

if [ "$(contar_tabla CATEGORIAS)" = "1" ]; then
  echo "TABLAS_INCOMPLETAS: la base tiene solo una parte de las tablas. Revisar a mano."
  exit 2
fi

for f in 01 02 03 04 07 08 11 12 13 14; do
  archivo=$(ls "$DIR"/"$f"-*.sql)
  echo "Aplicando $(basename "$archivo")"
  {
    echo "WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK"
    echo "ALTER SESSION SET CONTAINER=FREEPDB1;"
    cat "$archivo"
    echo
    echo "COMMIT;"
    echo "EXIT"
  } | sqlplus -s / as sysdba
  if [ "${PIPESTATUS[1]}" -ne 0 ]; then
    echo "FALLO_SCRIPT $f"
    exit 1
  fi
done

echo "TABLAS_CREADAS"
