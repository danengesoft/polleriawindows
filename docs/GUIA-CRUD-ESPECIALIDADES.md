# Guía: CRUD completo de Especialidades (igual que Clínicas)

Hoy la pantalla **Especialidades** solo lista. Esta guía la deja igual que **Clínicas**:

- botón **+ Nueva especialidad**;
- **Editar** y **Eliminar** en cada fila;
- un formulario que sirve para crear y para editar, con los errores visibles;
- *(opcional, Parte C)* la columna **Activa**.

**Cada paso trae el archivo completo.** No hay que buscar dónde va cada bloque: abres el archivo,
borras todo y pegas el código. Si el archivo no existe, lo creas.

Todo el código de esta guía se compiló y se probó en el navegador antes de escribirla.

## Antes de empezar

### Las partes A y B son obligatorias

**No son opciones a elegir**: son las dos mitades del mismo trabajo. Solo están separadas porque
cada una va en una carpeta distinta.

```text
Página (Parte B)                       Servidor (Parte A)                  Base de datos
─────────────────                      ──────────────────                  ─────────────
Botón "Guardar"  ──POST /api/v1/──►    EspecialidadController  ──►  Service  ──►  ESPECIALIDADES
                   clinica/especialidades
Mensaje "Especialidad creada" ◄────    responde 201 con los datos
```

| Lo que haces | Lo que ves en la página |
|---|---|
| Solo la **A** (backend) | Nada cambia: no hay botones. Solo funcionaría desde Postman o Swagger |
| Solo la **B** (frontend) | Ves los botones, pero al guardar sale un error, porque el servidor no sabe crear ni editar |
| **A + B** | ✅ Funciona completo |

### Orden

1. **Parte A** (7 archivos del backend). Al terminar, compila:

   ```bash
   cd C:\polleriawindows\polleria-backend
   ```

   ```bash
   .\mvnw.cmd -q -DskipTests package
   ```

   Si no muestra nada, compiló bien.

2. **Parte B** (7 archivos del frontend).
3. **Probar** (al final de la guía).
4. **Parte C**, solo si quieres la columna "Activa". Es opcional.

### La base de datos

Para las partes A y B **no se toca**: la tabla `ESPECIALIDADES` ya existe. Solo la Parte C
necesita un cambio (paso C1).

### Resumen de archivos

Todas las rutas parten de `C:\polleriawindows\`.

| # | Archivo | Acción |
|---|---|---|
| A1 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadRequest.java` | 📄 Nuevo |
| A2 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\mapper\EspecialidadMapper.java` | ✏️ Reemplazar todo |
| A3 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\repository\EspecialidadRepository.java` | ✏️ Reemplazar todo |
| A4 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\repository\ConsultorioRepository.java` | ✏️ Reemplazar todo |
| A5 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadService.java` | ✏️ Reemplazar todo |
| A6 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadServiceImpl.java` | ✏️ Reemplazar todo |
| A7 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\controller\EspecialidadController.java` | ✏️ Reemplazar todo |
| B1 | `polleria-frontend\src\app\features\clinica\clinica.model.ts` | ✏️ Reemplazar todo |
| B2 | `polleria-frontend\src\app\features\clinica\clinica-service.ts` | ✏️ Reemplazar todo |
| B3 | `polleria-frontend\src\app\features\clinica\especialidad-list.ts` | ✏️ Reemplazar todo |
| B4 | `polleria-frontend\src\app\features\clinica\especialidad-list.html` | 📄 Nuevo |
| B5 | `polleria-frontend\src\app\features\clinica\especialidad-form.ts` | 📄 Nuevo |
| B6 | `polleria-frontend\src\app\features\clinica\especialidad-form.html` | 📄 Nuevo |
| B7 | `polleria-frontend\src\app\app.routes.ts` | ✏️ Reemplazar todo |

No hace falta tocar `SecurityConfig.java`: la regla `/api/v1/clinica/**` ya cubre las rutas nuevas.

---

## Parte A. Backend

### A1. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadRequest.java`

Crea el archivo en esa carpeta (clic derecho en la carpeta → **New File**) y pega el código.

Son los datos que llegan al crear o editar. El `id` no va, porque lo genera la base de datos. Los límites `max = 10` y `max = 100` son los mismos que tienen las columnas `CODIGO VARCHAR(10)` y `NOMBRE VARCHAR(100)`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspecialidadRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10, message = "El código admite hasta 10 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    private String nombre;
}
```

### A2. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\mapper\EspecialidadMapper.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agregan `toEntity` (request → entidad) y `actualizar` (copia el request sobre una entidad existente). `@Mapping(target = "id", ignore = true)` es obligatorio: el request no trae `id` y `unmappedTargetPolicy = ERROR` no deja compilar si falta.

```java
package pe.edu.upeu.polleria.clinica.especialidad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadRequest;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EspecialidadMapper {

    EspecialidadResponse toResponse(Especialidad especialidad);

    @Mapping(target = "id", ignore = true)
    Especialidad toEntity(EspecialidadRequest request);

    @Mapping(target = "id", ignore = true)
    void actualizar(EspecialidadRequest request, @MappingTarget Especialidad especialidad);
}
```

### A3. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\repository\EspecialidadRepository.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Dos métodos para que el código no se repita. El segundo se usa al **editar**: excluye la especialidad que estás editando.

```java
package pe.edu.upeu.polleria.clinica.especialidad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
```

### A4. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\repository\ConsultorioRepository.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega `existsByEspecialidades_Id` al final. Sirve para no borrar una especialidad asignada a un consultorio, porque la clave foránea lo impediría. El `_` le dice a Spring Data que entre a la lista `especialidades` y compare su `id`.

```java
package pe.edu.upeu.polleria.clinica.consultorio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {

    // Trae la clínica y las especialidades en la misma consulta (evita el problema N+1).
    @Override
    @EntityGraph(attributePaths = {"clinica", "especialidades"})
    List<Consultorio> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = {"clinica", "especialidades"})
    Optional<Consultorio> findById(Long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByClinicaId(Long clinicaId);

    // ¿Algún consultorio tiene asignada esta especialidad? (recorre la lista especialidades)
    boolean existsByEspecialidades_Id(Long especialidadId);
}
```

### A5. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadService.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

La interfaz con los 5 métodos del CRUD.

```java
package pe.edu.upeu.polleria.clinica.especialidad.service;

import java.util.List;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadRequest;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;

public interface EspecialidadService {
    List<EspecialidadResponse> listar();

    EspecialidadResponse obtener(Long id);

    EspecialidadResponse crear(EspecialidadRequest request);

    EspecialidadResponse actualizar(Long id, EspecialidadRequest request);

    void eliminar(Long id);
}
```

### A6. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadServiceImpl.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

La lógica. `normalizar` deja el código en mayúsculas (`" e-06 "` se guarda como `E-06`). Un código repetido responde **409**; borrar una especialidad asignada también responde **409**, con un mensaje que la página muestra; un id inexistente responde **404**.

```java
package pe.edu.upeu.polleria.clinica.especialidad.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.consultorio.repository.ConsultorioRepository;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadRequest;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;
import pe.edu.upeu.polleria.clinica.especialidad.mapper.EspecialidadMapper;
import pe.edu.upeu.polleria.clinica.especialidad.repository.EspecialidadRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository repository;
    private final ConsultorioRepository consultorioRepository;
    private final EspecialidadMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar() {
        return repository.findAll(Sort.by("codigo")).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadResponse obtener(Long id) {
        return mapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public EspecialidadResponse crear(EspecialidadRequest request) {
        normalizar(request);
        if (repository.existsByCodigoIgnoreCase(request.getCodigo()))
            throw new BusinessConflictException("Ya existe una especialidad con el código " + request.getCodigo());
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional
    public EspecialidadResponse actualizar(Long id, EspecialidadRequest request) {
        Especialidad especialidad = buscarOFallar(id);
        normalizar(request);
        if (repository.existsByCodigoIgnoreCaseAndIdNot(request.getCodigo(), id))
            throw new BusinessConflictException("Ya existe una especialidad con el código " + request.getCodigo());
        mapper.actualizar(request, especialidad);
        return mapper.toResponse(repository.save(especialidad));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Especialidad especialidad = buscarOFallar(id);
        // La FK de CONSULTORIO_ESPECIALIDAD impediría borrarla: se avisa con un mensaje claro.
        if (consultorioRepository.existsByEspecialidades_Id(id))
            throw new BusinessConflictException("No se puede eliminar: la especialidad está asignada a un consultorio");
        repository.delete(especialidad);
    }

    private Especialidad buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + id));
    }

    private void normalizar(EspecialidadRequest request) {
        request.setCodigo(request.getCodigo().strip().toUpperCase());
        request.setNombre(request.getNombre().strip());
    }
}
```

### A7. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\controller\EspecialidadController.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Los 5 endpoints REST: GET lista, GET por id, POST, PUT y DELETE. Son los mismos que en `ClinicaController`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadRequest;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.service.EspecialidadService;

@RestController
@RequestMapping("/api/v1/clinica/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService service;

    @GetMapping
    public ResponseEntity<List<EspecialidadResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) {
        EspecialidadResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/clinica/especialidades/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Parte B. Frontend (Angular)

### B1. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica.model.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega la interfaz `EspecialidadRequest` (lo que se envía al guardar). El resto del archivo queda igual.

```ts
// Modelos del módulo clínica: lo que el backend devuelve (Response) y lo que recibe (Request).

export interface Clinica {
  id: number;
  ruc: string;
  razonSocial: string;
  telefono: string | null;
  activo: boolean;
}

export interface ClinicaRequest {
  ruc: string;
  razonSocial: string;
  telefono: string | null;
  activo: boolean;
}

export interface Especialidad {
  id: number;
  codigo: string;
  nombre: string;
}

export interface EspecialidadRequest {
  codigo: string;
  nombre: string;
}

export const ESTADOS_CONSULTORIO = ['DISPONIBLE', 'OCUPADO', 'MANTENIMIENTO'] as const;
export type EstadoConsultorio = (typeof ESTADOS_CONSULTORIO)[number];

// Cabecera (consultorio) con sus líneas (especialidades).
export interface Consultorio {
  id: number;
  clinicaId: number;
  clinicaRazonSocial: string;
  codigo: string;
  tarifaHora: number;
  estado: EstadoConsultorio;
  especialidades: Especialidad[];
}

export interface ConsultorioRequest {
  clinicaId: number;
  codigo: string;
  tarifaHora: number;
  estado: EstadoConsultorio;
  especialidadIds: number[];
}
```

### B2. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica-service.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agregan `obtenerEspecialidad`, `crearEspecialidad`, `actualizarEspecialidad` y `eliminarEspecialidad`. `listarEspecialidades` conserva su nombre, así que la pantalla de **Nuevo consultorio** sigue funcionando igual.

```ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import {
  Clinica,
  ClinicaRequest,
  Consultorio,
  ConsultorioRequest,
  Especialidad,
  EspecialidadRequest,
} from './clinica.model';

// Un solo servicio para las tres pantallas del módulo: /api/v1/clinica/...
@Injectable({ providedIn: 'root' })
export class ClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly clinicas = '/api/v1/clinica/clinicas';
  private readonly consultorios = '/api/v1/clinica/consultorios';
  private readonly especialidades = '/api/v1/clinica/especialidades';

  listarClinicas() {
    return this.http.get<Clinica[]>(this.api.buildUrl(this.clinicas));
  }
  obtenerClinica(id: number) {
    return this.http.get<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }
  crearClinica(data: ClinicaRequest) {
    return this.http.post<Clinica>(this.api.buildUrl(this.clinicas), data);
  }
  actualizarClinica(id: number, data: ClinicaRequest) {
    return this.http.put<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`), data);
  }
  eliminarClinica(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }

  listarEspecialidades() {
    return this.http.get<Especialidad[]>(this.api.buildUrl(this.especialidades));
  }
  obtenerEspecialidad(id: number) {
    return this.http.get<Especialidad>(this.api.buildUrl(`${this.especialidades}/${id}`));
  }
  crearEspecialidad(data: EspecialidadRequest) {
    return this.http.post<Especialidad>(this.api.buildUrl(this.especialidades), data);
  }
  actualizarEspecialidad(id: number, data: EspecialidadRequest) {
    return this.http.put<Especialidad>(this.api.buildUrl(`${this.especialidades}/${id}`), data);
  }
  eliminarEspecialidad(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.especialidades}/${id}`));
  }

  listarConsultorios() {
    return this.http.get<Consultorio[]>(this.api.buildUrl(this.consultorios));
  }
  obtenerConsultorio(id: number) {
    return this.http.get<Consultorio>(this.api.buildUrl(`${this.consultorios}/${id}`));
  }
  crearConsultorio(data: ConsultorioRequest) {
    return this.http.post<Consultorio>(this.api.buildUrl(this.consultorios), data);
  }
  eliminarConsultorio(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.consultorios}/${id}`));
  }
}
```

### B3. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\especialidad-list.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

La lógica del listado con el botón **Eliminar**. Antes tenía el HTML dentro (`template:`); ahora usa un archivo aparte (`templateUrl`), que creas en el paso B4.

```ts
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmarEliminacion } from '../../shared/confirmar-eliminacion';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Especialidad } from './clinica.model';

@Component({
  selector: 'app-especialidad-list',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './especialidad-list.html',
})
export class EspecialidadList {
  private readonly service = inject(ClinicaService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly datos = signal<Especialidad[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly aviso = signal((history.state?.['aviso'] as string) || '');

  constructor() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.service
      .listarEspecialidades()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.datos.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(mensajeHttp(err));
          this.loading.set(false);
        },
      });
  }

  eliminar(item: Especialidad) {
    this.dialog
      .open(ConfirmarEliminacion, { data: item.nombre, width: '26rem', maxWidth: 'calc(100vw - 2rem)' })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        if (confirmado !== true) return;
        this.error.set('');
        this.aviso.set('');
        this.service
          .eliminarEspecialidad(item.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.aviso.set('Especialidad eliminada.');
              this.cargar();
            },
            error: (err) => this.error.set(mensajeHttp(err)),
          });
      });
  }
}
```

### B4. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\especialidad-list.html`

Crea el archivo en esa carpeta (clic derecho en la carpeta → **New File**) y pega el código.

La tabla con el botón **+ Nueva especialidad** y, en cada fila, **Editar** y **Eliminar**.

```html
<div class="page-heading">
  <h1>Especialidades</h1>
  <a matButton="filled" routerLink="/interno/clinica/especialidades/nuevo">+ Nueva especialidad</a>
</div>

@if (aviso()) {
  <p role="status" class="notice">{{ aviso() }}</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}
@if (loading()) {
  <p role="status">Cargando especialidades…</p>
}

<div class="table-region" [attr.aria-busy]="loading()">
  <table class="data-table">
    <caption class="sr-only">Listado de especialidades</caption>
    <thead>
      <tr>
        <th scope="col">Código</th>
        <th scope="col">Nombre</th>
        <th scope="col">Acciones</th>
      </tr>
    </thead>
    <tbody>
      @for (item of datos(); track item.id) {
        <tr>
          <td data-label="Código">{{ item.codigo }}</td>
          <td data-label="Nombre">{{ item.nombre }}</td>
          <td data-label="Acciones" class="row-actions">
            <a matButton [routerLink]="['/interno/clinica/especialidades', item.id, 'editar']">Editar</a>
            <button matButton class="danger" type="button" (click)="eliminar(item)">Eliminar</button>
          </td>
        </tr>
      } @empty {
        @if (!loading() && !error()) {
          <tr><td class="empty" colspan="3">No hay especialidades registradas.</td></tr>
        }
      }
    </tbody>
  </table>
</div>
```

### B5. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\especialidad-form.ts`

Crea el archivo en esa carpeta (clic derecho en la carpeta → **New File**) y pega el código.

Un solo formulario para **crear** (`/nuevo`) y **editar** (`/:id/editar`). `markAllAsTouched()` hace que se vean los errores debajo de cada campo al pulsar **Guardar**.

```ts
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { mensajeHttp } from '../../shared/http-error';
import { textoObligatorio } from '../../shared/form-validation';
import { ClinicaService } from './clinica-service';
import { EspecialidadRequest } from './clinica.model';

// La misma pantalla sirve para crear (/nuevo) y editar (/:id/editar).
@Component({
  selector: 'app-especialidad-form',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './especialidad-form.html',
})
export class EspecialidadForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClinicaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly id = signal<number | null>(null);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    codigo: ['', [textoObligatorio, Validators.maxLength(10)]],
    nombre: ['', [textoObligatorio, Validators.maxLength(100)]],
  });

  constructor() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param === null) return; // modo crear
    const id = Number(param);
    this.id.set(id);
    this.loading.set(true);
    this.service
      .obtenerEspecialidad(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (e) => {
          this.form.patchValue({ codigo: e.codigo, nombre: e.nombre });
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
  }

  mensaje(campo: 'codigo' | 'nombre'): string {
    const control = this.form.controls[campo];
    if (!control.touched || !control.errors) return '';
    if (control.errors['maxlength']) return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
    return campo === 'codigo' ? 'El código es obligatorio' : 'El nombre es obligatorio';
  }

  guardar() {
    if (this.guardando()) return;
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // muestra los errores debajo de cada campo
      return;
    }
    const v = this.form.getRawValue();
    const data: EspecialidadRequest = { codigo: v.codigo.trim().toUpperCase(), nombre: v.nombre.trim() };
    this.guardando.set(true);
    const id = this.id();
    const peticion =
      id === null ? this.service.crearEspecialidad(data) : this.service.actualizarEspecialidad(id, data);
    peticion.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () =>
        this.router.navigate(['/interno/clinica/especialidades'], {
          state: { aviso: id === null ? 'Especialidad creada.' : 'Cambios guardados.' },
        }),
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeHttp(err));
      },
    });
  }
}
```

### B6. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\especialidad-form.html`

Crea el archivo en esa carpeta (clic derecho en la carpeta → **New File**) y pega el código.

La pantalla del formulario. `@if (!loading())` muestra el formulario solo cuando ya cargó la especialidad.

```html
<h1>{{ id() === null ? 'Nueva especialidad' : 'Editar especialidad' }}</h1>
@if (loading()) {
  <p role="status">Cargando especialidad…</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}

@if (!loading()) {
  <form [formGroup]="form" (ngSubmit)="guardar()" class="resource-form" novalidate>
    <mat-form-field appearance="outline">
      <mat-label>Código</mat-label>
      <input matInput formControlName="codigo" maxlength="10" placeholder="E-06" />
      <mat-error>× {{ mensaje('codigo') }}</mat-error>
    </mat-form-field>

    <mat-form-field appearance="outline">
      <mat-label>Nombre</mat-label>
      <input matInput formControlName="nombre" maxlength="100" />
      <mat-error>× {{ mensaje('nombre') }}</mat-error>
    </mat-form-field>

    <div class="form-actions">
      <a matButton routerLink="/interno/clinica/especialidades">Cancelar</a>
      <button matButton="filled" type="submit" [disabled]="guardando()">
        {{ guardando() ? 'Guardando…' : 'Guardar' }}
      </button>
    </div>
  </form>
}
```

### B7. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\app.routes.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Son todas las rutas del sistema más las dos nuevas: `clinica/especialidades/nuevo` y `clinica/especialidades/:id/editar`.

> ⚠️ Este archivo tiene las rutas de **todo** el sistema. Pega el contenido completo; no borres ni dejes nada a medias.

```ts
import { authGuard, authChildGuard } from './core/guards/auth-guard';
import { rolGuard,inicioInternoGuard,publicoGuard } from './core/guards/rol-guard';
import { INTERNOS, OPERATIVOS } from './core/auth/auth-service';
import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'inicio',canActivate:[publicoGuard],title:'Pollería Neyos | Pollos y Parrillas',loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'destacados',canActivate:[publicoGuard],title:'Destacados de la carta | Polleria Neyos',data:{seccion:'destacados'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'promociones',canActivate:[publicoGuard],title:'Promociones | Polleria Neyos',data:{seccion:'promociones'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'',pathMatch:'full',redirectTo:'inicio'},
  {path:'acceso',canActivate:[publicoGuard],loadComponent:()=>import('./features/publico/acceso').then(m=>m.Acceso)},
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: 'sin-permiso',
    loadComponent: () => import('./core/auth/sin-permiso').then((m) => m.SinPermiso),
  },
  {
    path: 'interno',
    canActivate: [authGuard, rolGuard(...INTERNOS)],
    canActivateChild: [authChildGuard, rolGuard(...INTERNOS)],
    loadComponent: () => import('./core/layout/interno/interno').then((m) => m.Interno),
    children: [
      {path:'ubicacion',redirectTo:'portada',pathMatch:'full'},
      {path:'portada',canActivate:[rolGuard('ADMINISTRADOR')],title:'Local y página de inicio | Pollería Neyos',loadComponent:()=>import('./features/contenido/banners').then(m=>m.Banners)},
      {path:'caja',canActivate:[rolGuard('CAJERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Caja)},
      {path:'operaciones',canActivate:[rolGuard('OPERACIONES','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Operaciones)},
      {path:'mesero',canActivate:[rolGuard('MESERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Mesero)},
      // Módulo clínica (examen final práctico v7): solo ADMINISTRADOR.
      {path:'clinica',redirectTo:'clinica/clinicas',pathMatch:'full'},
      {path:'clinica/clinicas',canActivate:[rolGuard('ADMINISTRADOR')],title:'Clínicas | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-list').then(m=>m.ClinicaList)},
      {path:'clinica/clinicas/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nueva clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/clinicas/:id/editar',canActivate:[rolGuard('ADMINISTRADOR')],title:'Editar clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/especialidades',canActivate:[rolGuard('ADMINISTRADOR')],title:'Especialidades | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-list').then(m=>m.EspecialidadList)},
      {path:'clinica/especialidades/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nueva especialidad | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-form').then(m=>m.EspecialidadForm)},
      {path:'clinica/especialidades/:id/editar',canActivate:[rolGuard('ADMINISTRADOR')],title:'Editar especialidad | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-form').then(m=>m.EspecialidadForm)},
      {path:'clinica/consultorios',canActivate:[rolGuard('ADMINISTRADOR')],title:'Consultorios | Pollería Neyos',loadComponent:()=>import('./features/clinica/consultorio-list').then(m=>m.ConsultorioList)},
      {path:'clinica/consultorios/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nuevo consultorio | Pollería Neyos',loadComponent:()=>import('./features/clinica/consultorio-form').then(m=>m.ConsultorioForm)},
      {path:'clinica/consultorios/:id',canActivate:[rolGuard('ADMINISTRADOR')],title:'Consultorio | Pollería Neyos',loadComponent:()=>import('./features/clinica/consultorio-detalle').then(m=>m.ConsultorioDetalle)},
      {
        path: 'reportes',
        canActivate: [rolGuard('ADMINISTRADOR')],
        title: 'Reportes | Pollería Neyos',
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: '',
        pathMatch: 'full',
        canActivate:[inicioInternoGuard],
        title: 'Portal interno | Pollería Neyos',
        loadComponent: () => import('./core/inicio/inicio-interno').then((m) => m.InicioInterno),
      },
      {
        path: 'categorias',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-list').then((m) => m.CategoriaList),
      },
      {
        path: 'categorias/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'categorias/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'productos',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-list').then((m) => m.ProductoList),
      },
      {
        path: 'productos/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'productos/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'clientes',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'pedidos',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Pedidos | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-list').then((m) => m.PedidoList),
      },
      {
        path: 'pedidos/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Nuevo pedido | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-form').then((m) => m.PedidoForm),
      },
      {
        path: 'pedidos/:id',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Detalle del pedido | Pollería Neyos',
        loadComponent: () =>
          import('./features/pedidos/pedido-detalle').then((m) => m.PedidoDetalle),
      },

    ],
  },
  {
    path: '',
    canActivateChild: [publicoGuard],
    loadComponent: () => import('./core/layout/publico/publico').then((m) => m.Publico),
    children: [
      {
        path: 'catalogo',
        loadComponent: () =>
          import('./features/publico/catalogo-publico').then((m) => m.CatalogoPublico),
      },
      {
        path: 'carrito',
        loadComponent: () => import('./features/publico/carrito').then((m) => m.Carrito),
      },
      {
        path: 'perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/perfil').then((m) => m.Perfil),
      },
      {
        path: 'checkout',
        loadComponent: () => import('./features/publico/checkout').then((m) => m.Checkout),
      },
      {path:'seguimiento/:id',data:{web:true},loadComponent:()=>import('./features/publico/mi-pedido').then(m=>m.MiPedido)},
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'mis-pedidos/:id',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mi-pedido').then((m) => m.MiPedido),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'inicio',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

---

## Probar

1. Reconstruye y levanta, desde `C:\polleriawindows`. Si estás en modo desarrollo
   (`desarrollo.bat`), no hace falta: los cambios se ven al guardar.

   ```bash
   docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml up -d --build --wait
   ```

2. Entra con `admin@polleria.pe` y abre **Clínica → Especialidades**. Si no ves el botón
   **+ Nueva especialidad**, pulsa `Ctrl + Shift + R`.

| Prueba | Resultado esperado |
|---|---|
| **+ Nueva especialidad** → Guardar con todo vacío | Debajo de los campos: *"× El código es obligatorio"* y *"× El nombre es obligatorio"* |
| Crear `e-06` / `Dermatologia` | Vuelve al listado con "Especialidad creada." y aparece **E-06** (en mayúsculas) |
| Crear otra con código `E-06` | Mensaje arriba: *"Ya existe una especialidad con el código E-06"* |
| **Editar** E-06 y cambiar el nombre | "Cambios guardados." y el nombre nuevo en la tabla |
| **Eliminar** E-06 → Confirmar | "Especialidad eliminada." |
| **Eliminar** E-01 (está asignada a CON-101) | *"No se puede eliminar: la especialidad está asignada a un consultorio"* |

---

## Parte C (opcional). Columna "Activa", como en Clínicas

Hazla **solo después** de que A y B funcionen. La tabla `ESPECIALIDADES` del examen no tiene
columna `ACTIVO`, así que primero se agrega en la base de datos (C1) y luego se reemplazan
7 archivos (C2 a C8). Varios son los mismos de la Parte B, ahora con `activo`.

| # | Archivo | Acción |
|---|---|---|
| C1 | Base de datos | Ejecutar un `ALTER TABLE` |
| C2 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\entity\Especialidad.java` | ✏️ Reemplazar todo |
| C3 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadRequest.java` | ✏️ Reemplazar todo |
| C4 | `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadResponse.java` | ✏️ Reemplazar todo |
| C5 | `polleria-frontend\src\app\features\clinica\clinica.model.ts` | ✏️ Reemplazar todo |
| C6 | `polleria-frontend\src\app\features\clinica\especialidad-list.html` | ✏️ Reemplazar todo |
| C7 | `polleria-frontend\src\app\features\clinica\especialidad-form.ts` | ✏️ Reemplazar todo |
| C8 | `polleria-frontend\src\app\features\clinica\especialidad-form.html` | ✏️ Reemplazar todo |

### C1. Base de datos: agregar la columna

> **Haz C1 antes que todo lo demás.** El backend revisa al arrancar que la entidad coincida con la
> tabla (`ddl-auto: validate`). Si la entidad tiene `activo` y la tabla no, el backend **no arranca**.

En PowerShell:

```powershell
$DB = 'polleria-prodtest-postgres-1'
@'
ALTER TABLE EXA_CLINICA.ESPECIALIDADES
    ADD COLUMN ACTIVO SMALLINT DEFAULT 1 NOT NULL
    CONSTRAINT CK_ESPECIALIDAD_ACTIVO CHECK (ACTIVO IN (0, 1));
'@ | docker exec -i $DB psql -U polleria -d polleria -v ON_ERROR_STOP=1
```

Debe responder `ALTER TABLE`. Las 5 especialidades que ya existen quedan con `ACTIVO = 1` gracias
al `DEFAULT 1`.

> Para que una instalación nueva ya tenga la columna, pega también esa sentencia `ALTER TABLE` al
> final de `polleria-backend\scripts\02-clinica.sql`.

### C2. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\entity\Especialidad.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega el campo `activo`, igual que en `Clinica`: en la base es `SMALLINT` 0/1 y en Java es `Boolean`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.NumericBooleanConverter;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ESPECIALIDADES", schema = "EXA_CLINICA")
@Getter
@Setter
@NoArgsConstructor
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODIGO", nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    // La columna es SMALLINT 0/1; en Java se trabaja como Boolean (igual que en Clinica).
    @Convert(converter = NumericBooleanConverter.class)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;
}
```

### C3. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadRequest.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega `activo` con `@NotNull`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspecialidadRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10, message = "El código admite hasta 10 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    private String nombre;

    @NotNull
    private Boolean activo;
}
```

### C4. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadResponse.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega `activo`. El mapper **no cambia**: MapStruct copia el campo solo, porque se llama igual.

```java
package pe.edu.upeu.polleria.clinica.especialidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private Boolean activo;
}
```

### C5. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica.model.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega `activo: boolean` en `Especialidad` y en `EspecialidadRequest`.

```ts
// Modelos del módulo clínica: lo que el backend devuelve (Response) y lo que recibe (Request).

export interface Clinica {
  id: number;
  ruc: string;
  razonSocial: string;
  telefono: string | null;
  activo: boolean;
}

export interface ClinicaRequest {
  ruc: string;
  razonSocial: string;
  telefono: string | null;
  activo: boolean;
}

export interface Especialidad {
  id: number;
  codigo: string;
  nombre: string;
  activo: boolean;
}

export interface EspecialidadRequest {
  codigo: string;
  nombre: string;
  activo: boolean;
}

export const ESTADOS_CONSULTORIO = ['DISPONIBLE', 'OCUPADO', 'MANTENIMIENTO'] as const;
export type EstadoConsultorio = (typeof ESTADOS_CONSULTORIO)[number];

// Cabecera (consultorio) con sus líneas (especialidades).
export interface Consultorio {
  id: number;
  clinicaId: number;
  clinicaRazonSocial: string;
  codigo: string;
  tarifaHora: number;
  estado: EstadoConsultorio;
  especialidades: Especialidad[];
}

export interface ConsultorioRequest {
  clinicaId: number;
  codigo: string;
  tarifaHora: number;
  estado: EstadoConsultorio;
  especialidadIds: number[];
}
```

### C6. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\especialidad-list.html`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega la columna **Activa** (Sí/No).

```html
<div class="page-heading">
  <h1>Especialidades</h1>
  <a matButton="filled" routerLink="/interno/clinica/especialidades/nuevo">+ Nueva especialidad</a>
</div>

@if (aviso()) {
  <p role="status" class="notice">{{ aviso() }}</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}
@if (loading()) {
  <p role="status">Cargando especialidades…</p>
}

<div class="table-region" [attr.aria-busy]="loading()">
  <table class="data-table">
    <caption class="sr-only">Listado de especialidades</caption>
    <thead>
      <tr>
        <th scope="col">Código</th>
        <th scope="col">Nombre</th>
        <th scope="col">Activa</th>
        <th scope="col">Acciones</th>
      </tr>
    </thead>
    <tbody>
      @for (item of datos(); track item.id) {
        <tr>
          <td data-label="Código">{{ item.codigo }}</td>
          <td data-label="Nombre">{{ item.nombre }}</td>
          <td data-label="Activa">{{ item.activo ? 'Sí' : 'No' }}</td>
          <td data-label="Acciones" class="row-actions">
            <a matButton [routerLink]="['/interno/clinica/especialidades', item.id, 'editar']">Editar</a>
            <button matButton class="danger" type="button" (click)="eliminar(item)">Eliminar</button>
          </td>
        </tr>
      } @empty {
        @if (!loading() && !error()) {
          <tr><td class="empty" colspan="4">No hay especialidades registradas.</td></tr>
        }
      }
    </tbody>
  </table>
</div>
```

### C7. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\especialidad-form.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega `activo` al formulario, a la carga al editar y al guardar, y se importa `MatCheckboxModule`.

```ts
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { mensajeHttp } from '../../shared/http-error';
import { textoObligatorio } from '../../shared/form-validation';
import { ClinicaService } from './clinica-service';
import { EspecialidadRequest } from './clinica.model';

// La misma pantalla sirve para crear (/nuevo) y editar (/:id/editar).
@Component({
  selector: 'app-especialidad-form',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatCheckboxModule],
  templateUrl: './especialidad-form.html',
})
export class EspecialidadForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClinicaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly id = signal<number | null>(null);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    codigo: ['', [textoObligatorio, Validators.maxLength(10)]],
    nombre: ['', [textoObligatorio, Validators.maxLength(100)]],
    activo: [true],
  });

  constructor() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param === null) return; // modo crear
    const id = Number(param);
    this.id.set(id);
    this.loading.set(true);
    this.service
      .obtenerEspecialidad(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (e) => {
          this.form.patchValue({ codigo: e.codigo, nombre: e.nombre, activo: e.activo });
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
  }

  mensaje(campo: 'codigo' | 'nombre'): string {
    const control = this.form.controls[campo];
    if (!control.touched || !control.errors) return '';
    if (control.errors['maxlength']) return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
    return campo === 'codigo' ? 'El código es obligatorio' : 'El nombre es obligatorio';
  }

  guardar() {
    if (this.guardando()) return;
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // muestra los errores debajo de cada campo
      return;
    }
    const v = this.form.getRawValue();
    const data: EspecialidadRequest = {
      codigo: v.codigo.trim().toUpperCase(),
      nombre: v.nombre.trim(),
      activo: v.activo,
    };
    this.guardando.set(true);
    const id = this.id();
    const peticion =
      id === null ? this.service.crearEspecialidad(data) : this.service.actualizarEspecialidad(id, data);
    peticion.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () =>
        this.router.navigate(['/interno/clinica/especialidades'], {
          state: { aviso: id === null ? 'Especialidad creada.' : 'Cambios guardados.' },
        }),
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeHttp(err));
      },
    });
  }
}
```

### C8. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\especialidad-form.html`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega la casilla **Activa**.

```html
<h1>{{ id() === null ? 'Nueva especialidad' : 'Editar especialidad' }}</h1>
@if (loading()) {
  <p role="status">Cargando especialidad…</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}

@if (!loading()) {
  <form [formGroup]="form" (ngSubmit)="guardar()" class="resource-form" novalidate>
    <mat-form-field appearance="outline">
      <mat-label>Código</mat-label>
      <input matInput formControlName="codigo" maxlength="10" placeholder="E-06" />
      <mat-error>× {{ mensaje('codigo') }}</mat-error>
    </mat-form-field>

    <mat-form-field appearance="outline">
      <mat-label>Nombre</mat-label>
      <input matInput formControlName="nombre" maxlength="100" />
      <mat-error>× {{ mensaje('nombre') }}</mat-error>
    </mat-form-field>

    <mat-checkbox formControlName="activo">Activa</mat-checkbox>

    <div class="form-actions">
      <a matButton routerLink="/interno/clinica/especialidades">Cancelar</a>
      <button matButton="filled" type="submit" [disabled]="guardando()">
        {{ guardando() ? 'Guardando…' : 'Guardar' }}
      </button>
    </div>
  </form>
}
```

**Prueba:** reconstruye con el mismo comando de [Probar](#probar), edita **Nutricion**, desmarca
**Activa** y guarda. En el listado debe decir **No**.

---

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| Al compilar: `cannot find symbol ... EspecialidadRequest` | Falta el archivo nuevo de A1, o está en otra carpeta |
| Al compilar: `Unmapped target property: "id"` | El mapper de A2 no quedó completo |
| Al compilar: `No property 'especialidades' found` | El método de A4 se escribió distinto: debe ser exactamente `existsByEspecialidades_Id` |
| El backend no arranca: `missing column [activo]` | Hiciste C2 sin hacer C1 (falta la columna en la tabla) |
| Al pulsar "+ Nueva especialidad" vuelve al inicio | El archivo `app.routes.ts` de B7 no quedó completo |
| `NG2008: Could not find template file './especialidad-list.html'` | Falta el archivo nuevo de B4 |
| `NG8001: 'mat-checkbox' is not a known element` | El archivo de C7 no quedó completo |
| El navegador sigue mostrando la pantalla vieja | No reconstruiste, o falta `Ctrl + Shift + R` |
