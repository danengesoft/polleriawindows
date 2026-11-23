# Guía: CRUD completo de Especialidades (igual que Clínicas)

Hoy la pantalla **Especialidades** solo lista. Esta guía la deja igual que **Clínicas**:

- botón **+ Nueva especialidad**;
- **Editar** y **Eliminar** en cada fila;
- un formulario que sirve para crear y para editar, con los errores visibles;
- *(opcional, Parte C)* la columna **Activa**.

Todo el código de esta guía se compiló y se probó en el navegador antes de escribirla.

**Cómo leer cada paso:**

- **📄 ARCHIVO NUEVO:** crea el archivo en esa ruta y pega el código completo.
- **✏️ MODIFICAR:** el archivo ya existe; la guía dice qué agregar o qué reemplazar y en qué lugar.

Rutas base:

- Backend: `polleria-backend/src/main/java/pe/edu/upeu/polleria/clinica/`
- Frontend: `polleria-frontend/src/app/`

**Resumen de archivos:**

| # | Archivo | Acción |
|---|---|---|
| A1 | `clinica/especialidad/dto/EspecialidadRequest.java` | 📄 Nuevo |
| A2 | `clinica/especialidad/mapper/EspecialidadMapper.java` | ✏️ Modificar |
| A3 | `clinica/especialidad/repository/EspecialidadRepository.java` | ✏️ Modificar |
| A4 | `clinica/consultorio/repository/ConsultorioRepository.java` | ✏️ Modificar |
| A5 | `clinica/especialidad/service/EspecialidadService.java` | ✏️ Modificar |
| A6 | `clinica/especialidad/service/EspecialidadServiceImpl.java` | ✏️ Modificar (reemplazar todo) |
| A7 | `clinica/especialidad/controller/EspecialidadController.java` | ✏️ Modificar (reemplazar todo) |
| B1 | `features/clinica/clinica.model.ts` | ✏️ Modificar |
| B2 | `features/clinica/clinica-service.ts` | ✏️ Modificar |
| B3 | `features/clinica/especialidad-list.ts` | ✏️ Modificar (reemplazar todo) |
| B4 | `features/clinica/especialidad-list.html` | 📄 Nuevo |
| B5 | `features/clinica/especialidad-form.ts` | 📄 Nuevo |
| B6 | `features/clinica/especialidad-form.html` | 📄 Nuevo |
| B7 | `app.routes.ts` | ✏️ Modificar |

No hace falta tocar `SecurityConfig.java`: la regla `/api/v1/clinica/**` ya cubre las rutas nuevas.
Tampoco hace falta tocar la base de datos, salvo en la Parte C.

---

## Parte A. Backend

### A1. 📄 ARCHIVO NUEVO: `clinica/especialidad/dto/EspecialidadRequest.java`

Son los datos que llegan al crear o editar. El `id` no va: lo genera la base de datos.

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

> `max = 10` y `max = 100` son los mismos límites que tienen `CODIGO VARCHAR(10)` y
> `NOMBRE VARCHAR(100)` en la tabla.

### A2. ✏️ MODIFICAR: `clinica/especialidad/mapper/EspecialidadMapper.java`

Reemplaza **todo** el archivo por este. Se agregan `toEntity` y `actualizar`, iguales a los de
`ClinicaMapper`:

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

> `@Mapping(target = "id", ignore = true)`: el request no trae `id`. Sin esta línea, MapStruct
> marca error al compilar porque `unmappedTargetPolicy = ERROR`.

### A3. ✏️ MODIFICAR: `clinica/especialidad/repository/EspecialidadRepository.java`

Agrega **dentro** de la interfaz, que ahora está vacía, los dos métodos para validar que el
código no se repita:

```java
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
```

> El segundo método se usa al **editar**: excluye la especialidad que estás editando, así puedes
> guardarla sin cambiar su código.

### A4. ✏️ MODIFICAR: `clinica/consultorio/repository/ConsultorioRepository.java`

Agrega este método **debajo de** `boolean existsByClinicaId(Long clinicaId);`:

```java
    // ¿Algún consultorio tiene asignada esta especialidad? (recorre la lista especialidades)
    boolean existsByEspecialidades_Id(Long especialidadId);
```

> **Por qué hace falta:** si una especialidad está asignada a un consultorio, la FK de
> `CONSULTORIO_ESPECIALIDAD` impide borrarla. Con este método se avisa antes, con un mensaje
> claro. El `_` en `Especialidades_Id` le dice a Spring Data que entre a la lista
> `especialidades` y compare su `id`.

### A5. ✏️ MODIFICAR: `clinica/especialidad/service/EspecialidadService.java`

Reemplaza **todo** el archivo. Se agregan los cuatro métodos nuevos (`obtener`, `crear`,
`actualizar` y `eliminar`):

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

### A6. ✏️ MODIFICAR: `clinica/especialidad/service/EspecialidadServiceImpl.java`

Reemplaza **todo** el archivo. Sigue la misma forma que `ClinicaServiceImpl`:

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

Qué hace cada parte:

| Método | Qué hace |
|---|---|
| `normalizar` | Quita los espacios y pasa el código a mayúsculas: `" e-06 "` se guarda como `E-06` |
| `crear` y `actualizar` | Si el código ya existe, responden **409** (`BusinessConflictException`) |
| `eliminar` | Si la especialidad está asignada a un consultorio, responde **409** con un mensaje que la SPA muestra |
| `buscarOFallar` | Si el id no existe, responde **404** |

### A7. ✏️ MODIFICAR: `clinica/especialidad/controller/EspecialidadController.java`

Reemplaza **todo** el archivo. Son los mismos 5 endpoints que `ClinicaController`:

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

| Método | URL | Respuesta |
|---|---|---|
| GET | `/api/v1/clinica/especialidades` | 200 |
| GET | `/api/v1/clinica/especialidades/{id}` | 200 / 404 |
| POST | `/api/v1/clinica/especialidades` | 201 / 400 / 409 |
| PUT | `/api/v1/clinica/especialidades/{id}` | 200 / 400 / 404 / 409 |
| DELETE | `/api/v1/clinica/especialidades/{id}` | 204 / 404 / 409 |

**Comprueba que compila** desde `C:\polleriawindows\polleria-backend`:

```bash
.\mvnw.cmd -q -DskipTests package
```

Si no muestra nada, compiló bien.

---

## Parte B. Frontend (Angular)

### B1. ✏️ MODIFICAR: `features/clinica/clinica.model.ts`

Busca la interfaz `Especialidad` y agrega **justo debajo** la interfaz del request:

```ts
export interface Especialidad {
  id: number;
  codigo: string;
  nombre: string;
}

// ⬇️ NUEVO
export interface EspecialidadRequest {
  codigo: string;
  nombre: string;
}
```

### B2. ✏️ MODIFICAR: `features/clinica/clinica-service.ts`

**1) En el `import` de arriba**, agrega `EspecialidadRequest`:

```ts
import {
  Clinica,
  ClinicaRequest,
  Consultorio,
  ConsultorioRequest,
  Especialidad,
  EspecialidadRequest,   // ⬅️ NUEVO
} from './clinica.model';
```

**2) Debajo de** `private readonly consultorios = '/api/v1/clinica/consultorios';`, agrega:

```ts
  private readonly especialidades = '/api/v1/clinica/especialidades';
```

**3) Reemplaza** el método `listarEspecialidades()` por este bloque, que trae los 4 métodos nuevos:

```ts
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
```

> El formulario de **Nuevo consultorio** también usa `listarEspecialidades()`. Como el nombre del
> método no cambia, esa pantalla sigue funcionando igual.

### B3. ✏️ MODIFICAR: `features/clinica/especialidad-list.ts`

Reemplaza **todo** el archivo. Antes tenía el HTML dentro (`template:`); ahora usa un archivo
aparte (`templateUrl`), igual que `clinica-list.ts`:

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

### B4. 📄 ARCHIVO NUEVO: `features/clinica/especialidad-list.html`

Créalo en la misma carpeta que `especialidad-list.ts`:

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

### B5. 📄 ARCHIVO NUEVO: `features/clinica/especialidad-form.ts`

Es una sola pantalla para **crear** (`/nuevo`) y **editar** (`/:id/editar`), igual que `clinica-form.ts`:

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

### B6. 📄 ARCHIVO NUEVO: `features/clinica/especialidad-form.html`

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

> `@if (!loading())`: el formulario aparece solo cuando ya cargó la especialidad. Si no, lo que
> escribas antes de que lleguen los datos se borraría al llegar.

### B7. ✏️ MODIFICAR: `app.routes.ts`

Busca la línea de la ruta `clinica/especialidades` (la del listado). **Debajo de ella**, agrega
estas dos rutas:

```ts
      {path:'clinica/especialidades/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nueva especialidad | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-form').then(m=>m.EspecialidadForm)},
      {path:'clinica/especialidades/:id/editar',canActivate:[rolGuard('ADMINISTRADOR')],title:'Editar especialidad | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-form').then(m=>m.EspecialidadForm)},
```

Deben quedar **dentro** del bloque `children` de `interno`, junto a las otras rutas `clinica/...`.

---

## Probar

1. Reconstruye y levanta, desde `C:\polleriawindows`. Si estás en modo desarrollo
   (`desarrollo.bat`), no hace falta: los cambios se ven al guardar.

   ```bash
   docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml up -d --build --wait
   ```

2. Entra con `admin@polleria.pe` y abre **Clínica → Especialidades**.

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

La tabla `ESPECIALIDADES` del examen **no tiene** columna `ACTIVO`. Para agregarla hay que
cambiar la base de datos y 7 archivos. Haz primero las partes A y B.

### C1. Base de datos: agregar la columna

```powershell
$DB = 'polleria-prodtest-postgres-1'
@'
ALTER TABLE EXA_CLINICA.ESPECIALIDADES
    ADD COLUMN ACTIVO SMALLINT DEFAULT 1 NOT NULL
    CONSTRAINT CK_ESPECIALIDAD_ACTIVO CHECK (ACTIVO IN (0, 1));
'@ | docker exec -i $DB psql -U polleria -d polleria -v ON_ERROR_STOP=1
```

Las 5 especialidades que ya existen quedan con `ACTIVO = 1` gracias al `DEFAULT 1`.

> Para que una instalación nueva ya tenga la columna, agrega también esa sentencia al final de
> `polleria-backend/scripts/02-clinica.sql`.
>
> **Haz C1 antes de levantar el backend con C2.** Con `ddl-auto: validate`, si la entidad tiene
> `activo` y la tabla no, el backend **no arranca**.

### C2. ✏️ `clinica/especialidad/entity/Especialidad.java`

**Imports** (arriba, junto a los demás):

```java
import jakarta.persistence.Convert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.NumericBooleanConverter;
import org.hibernate.type.SqlTypes;
```

**Campo nuevo**, debajo de `private String nombre;`. Es idéntico al de `Clinica`:

```java
    @Convert(converter = NumericBooleanConverter.class)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;
```

### C3. ✏️ `clinica/especialidad/dto/EspecialidadRequest.java`

Agrega el import `import jakarta.validation.constraints.NotNull;` y, debajo de `nombre`, este campo:

```java
    @NotNull
    private Boolean activo;
```

### C4. ✏️ `clinica/especialidad/dto/EspecialidadResponse.java`

Agrega este campo debajo de `private String nombre;`:

```java
    private Boolean activo;
```

El mapper **no cambia**: MapStruct copia `activo` solo, porque se llama igual en la entidad y en
los DTO.

### C5. ✏️ `features/clinica/clinica.model.ts`

Agrega `activo: boolean;` en **las dos** interfaces:

```ts
export interface Especialidad {
  id: number;
  codigo: string;
  nombre: string;
  activo: boolean;   // ⬅️ NUEVO
}

export interface EspecialidadRequest {
  codigo: string;
  nombre: string;
  activo: boolean;   // ⬅️ NUEVO
}
```

### C6. ✏️ `features/clinica/especialidad-list.html`

En `<thead>`, **debajo de** `<th scope="col">Nombre</th>`:

```html
        <th scope="col">Activa</th>
```

En cada fila, **debajo de** `<td data-label="Nombre">{{ item.nombre }}</td>`:

```html
          <td data-label="Activa">{{ item.activo ? 'Sí' : 'No' }}</td>
```

Y cambia `colspan="3"` por `colspan="4"` en la fila de "No hay especialidades registradas".

### C7. ✏️ `features/clinica/especialidad-form.ts`

1. Agrega el import:

   ```ts
   import { MatCheckboxModule } from '@angular/material/checkbox';
   ```

2. Agrega `MatCheckboxModule` al final de `imports: [...]` dentro de `@Component`.

3. En el formulario, debajo de `nombre`:

   ```ts
       activo: [true],
   ```

4. En `patchValue` del constructor:

   ```ts
   this.form.patchValue({ codigo: e.codigo, nombre: e.nombre, activo: e.activo });
   ```

5. En `guardar()`, donde se arma `data`:

   ```ts
   const data: EspecialidadRequest = { codigo: v.codigo.trim().toUpperCase(), nombre: v.nombre.trim(), activo: v.activo };
   ```

### C8. ✏️ `features/clinica/especialidad-form.html`

**Justo antes de** `<div class="form-actions">`:

```html
    <mat-checkbox formControlName="activo">Activa</mat-checkbox>
```

**Prueba:** edita **Nutricion**, desmarca **Activa** y guarda. En el listado debe decir **No**.

---

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| Al compilar el backend: `Unmapped target property: "id"` | Falta `@Mapping(target = "id", ignore = true)` en `toEntity` o en `actualizar` |
| Al compilar el backend: `No property 'especialidades' found` | El método de A4 se escribió distinto. Debe ser exactamente `existsByEspecialidades_Id` |
| El backend no arranca: `missing column [activo]` | Hiciste C2 sin hacer C1 (falta la columna en la tabla) |
| Al pulsar "+ Nueva especialidad" vuelve al inicio | Faltan las rutas de B7 o quedaron fuera de `children` |
| `NG8001: 'mat-checkbox' is not a known element` | Falta `MatCheckboxModule` en `imports` (C7) |
| Eliminar no hace nada | Falta `MatDialog` o `ConfirmarEliminacion` en `especialidad-list.ts` |
