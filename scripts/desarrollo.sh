#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
# Evita dos procesos de vigilancia sobre el mismo proyecto.
exec 9>/tmp/polleria-desarrollo-watch.lock
if ! flock -n 9; then
  echo 'La vigilancia de Polleria ya está activa.'
  exit 0
fi
printf '%s\n' 'Recarga automática activa: http://127.0.0.1:18080' 'Angular actualiza el navegador al guardar. Los errores de compilación aparecen en la página.'
docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml -f compose.live.yml up -d --build --no-deps frontend-dev frontend
exec docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml -f compose.live.yml -f compose.watch.yml watch --no-up --prune=false backend
