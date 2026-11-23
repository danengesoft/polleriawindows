# Pollería Neyos — versión para Windows

Esta copia del proyecto está preparada para ejecutarse en **cualquier PC con Windows 10/11**
con **doble clic**, sin escribir comandos.

## Requisitos (una sola vez por PC)

| Requisito | Cómo comprobarlo |
|---|---|
| **Docker Desktop** instalado | Debe abrir y mostrar **Engine running** |
| Virtualización activada | Administrador de tareas → Rendimiento → CPU → **Virtualización: Habilitado** |
| 8 GB de RAM libres y 20 GB de disco | — |
| Internet la primera vez | Para descargar imágenes y compilar (o traer `polleria-imagenes.tar`, ver abajo) |

> Instalar Docker Desktop requiere **permisos de administrador**. No hace falta instalar Git,
> Java, Node, PostgreSQL ni nada más: todo corre dentro de Docker.

## Uso

1. Descomprime el ZIP en una carpeta **fuera de OneDrive y del Escritorio**, por ejemplo
   `C:\Proyectos\Polleria-windows`.
   *Si Windows marca el ZIP como descargado de internet: clic derecho en el ZIP → Propiedades →
   marcar **Desbloquear** → Aceptar, antes de descomprimir.*
2. Abre **Docker Desktop**.
3. Doble clic en **`iniciar.bat`**.

`iniciar.bat` hace todo solo, y se puede ejecutar todas las veces que quieras (solo hace lo que
falte):

1. Comprueba Docker (si Docker Desktop está cerrado, intenta abrirlo).
2. Crea el archivo de configuración `.env.prodtest` con contraseñas aleatorias (si no existe).
3. Prepara la configuración del login (Keycloak).
4. Carga las imágenes desde `polleria-imagenes.tar` si está, o compila el proyecto.
5. Levanta PostgreSQL y espera a que esté listo.
6. Crea las tablas de la pollería y del módulo clínica (solo la primera vez).
7. Levanta Keycloak, backend y frontend.
8. Crea los usuarios de prueba y abre el navegador en **http://127.0.0.1:18080**.

La **primera vez** tarda bastante (descargas + compilación): entre 5 y 15 minutos según
la PC y el internet. Las siguientes veces, 1 o 2 minutos.

## Usuarios de prueba

Contraseña de todos: **`Neyos2026!`**

| Correo | Rol | Entra a |
|---|---|---|
| `admin@polleria.pe` | ADMINISTRADOR | Todo el portal interno |
| `caja@polleria.pe` | CAJERO | Caja |
| `cocina@polleria.pe` | OPERACIONES | Operaciones (cocina) |
| `mesero@polleria.pe` | MESERO | Mesero |

Los **clientes** se registran solos desde la web pública ("Iniciar sesión" → registrarse).

> Son usuarios **solo para pruebas locales** (el sistema solo es accesible desde la misma PC).

## Archivos de esta carpeta

| Archivo | Para qué |
|---|---|
| `iniciar.bat` | Levanta todo el sistema |
| `detener.bat` | Apaga el sistema (los datos se conservan) |
| `desarrollo.bat` | Modo desarrollo: los cambios del código se ven al guardar. Deja la ventana abierta |
| `crear-usuario.bat` | Crea otro usuario del personal (pide correo, nombre, rol y contraseña) |
| `windows\` | Scripts que usan los `.bat` (no hace falta abrirlos) |

En **VS Code** también están como tareas: `Ctrl + Shift + P` → *Tasks: Run Task* → *Polleria: …*

## Llevarlo a otra PC sin depender del internet (opcional)

En una PC donde el sistema ya funcione, exporta las imágenes a un archivo:

```powershell
docker save -o polleria-imagenes.tar polleria-backend:prod-test-f19 polleria-frontend:prod-test-f19
```

Copia `polleria-imagenes.tar` a la carpeta del proyecto en la otra PC (junto a `iniciar.bat`):
`iniciar.bat` lo carga solo y **no necesita compilar**. Keycloak y PostgreSQL se
descargan si faltan.

## Proyecto solo para Windows

Este repositorio está preparado **solo para Windows con Docker Desktop**. Ya no incluye los
scripts de Linux/Fedora ni los de publicación en un servidor de producción.

- `.gitattributes` evita que Git cambie los saltos de línea en Windows (si cambian, el backend
  no compila). Los `.sh` de `windows\` no se ejecutan en Windows: se copian y corren **dentro**
  de los contenedores.
- Las tareas de `.vscode/tasks.json` usan los scripts de Windows y **no** se ejecutan solas al
  abrir la carpeta.
- Para compilar el backend fuera de Docker usa `polleria-backend\mvnw.cmd` (necesita JDK 21).
- `production\nginx\https.conf.template` no se usa en Windows, pero lo menciona
  `compose.prod.yml`: no lo borres.

## Problemas comunes

| Síntoma | Solución |
|---|---|
| La ventana se cierra o dice que los scripts están deshabilitados | Ejecuta el `.bat` (no el `.ps1`). Si persiste: desbloquea el ZIP (paso 1 de Uso) y descomprime de nuevo |
| "Docker no está instalado" / "Docker no responde" | Instala o abre Docker Desktop y espera a **Engine running** |
| Docker Desktop no inicia: falta virtualización o WSL | Activar la virtualización en la BIOS; en PowerShell como administrador: `wsl --install` y reiniciar |
| Falla en el paso 7 por el puerto `18080` en uso | Cierra el programa que lo usa o reinicia Docker Desktop |
| Falla en el paso 7 por tiempo de espera | La PC es lenta: ejecuta `iniciar.bat` otra vez (continúa donde quedó) |
| "TABLAS_INCOMPLETAS" | La creación de tablas se cortó a la mitad. Pide ayuda antes de seguir |
| Entra con un usuario pero aparece **"sin permiso"** | El usuario no tiene rol o correo verificado: créalo con `crear-usuario.bat` |
| Borraste `.env.prodtest` y ya nada arranca | Las bases de datos usan las contraseñas del archivo anterior. Restáuralo; si no lo tienes, borra los datos en Docker Desktop → Volumes (los que empiezan con `polleria-prodtest`) y ejecuta `iniciar.bat` |

**Ver los errores de un servicio** (en PowerShell, dentro de la carpeta del proyecto; cambia
`backend` por `keycloak`, `postgres` o `frontend`):

```powershell
docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml logs --tail 100 backend
```

## Base de datos: PostgreSQL

Desde esta versión el sistema usa **PostgreSQL 17** (contenedor `polleria-prodtest-postgres-1`),
con la base `polleria` y el usuario `polleria`. Tiene dos esquemas:

| Esquema | Contenido | Script |
|---|---|---|
| `polleria` | Tablas de la pollería | `polleria-backend\scripts\01-polleria.sql` |
| `exa_clinica` | Módulo clínica (examen v7) | `polleria-backend\scripts\02-clinica.sql` |

`iniciar.bat` crea los dos esquemas la primera vez. Si venías de la versión con Oracle,
`iniciar.bat` agrega sola la clave `POLLERIA_DB_PASSWORD` a `.env.prodtest` y apaga el
contenedor de Oracle. **Los datos de Oracle no se copian**: la base nueva empieza vacía.
Cuando ya no los necesites, borra el volumen viejo en Docker Desktop → Volumes
(`polleria-prodtest_oracle-data`).

Listar las tablas del módulo clínica (como en el script 3 del examen):

```powershell
$DB = 'polleria-prodtest-postgres-1'
@'
SELECT table_name FROM information_schema.tables WHERE table_schema = 'exa_clinica' ORDER BY 1;
'@ | docker exec -i $DB psql -U polleria -d polleria
```

Abrir una consola SQL:

```powershell
docker exec -it polleria-prodtest-postgres-1 psql -U polleria -d polleria
```

## Módulo clínica

En el portal interno, como **ADMINISTRADOR**, sección **Clínica** del menú:

| Pantalla | Ruta |
|---|---|
| Clínicas (listar, crear, editar, eliminar) | `/interno/clinica/clinicas` |
| Especialidades (precargadas) | `/interno/clinica/especialidades` |
| Consultorios con sus especialidades | `/interno/clinica/consultorios` |

La guía que explica cómo está construido, archivo por archivo, está en
`docs\GUIA-MODULO-CLINICA.md`.
